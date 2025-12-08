#!/bin/bash
set -e

cd ..

### Build Docker Image
image=kg2pg:dockerImage
docker build . -t $image

### Clear Cache
echo "Clearing cache"
[ "$EUID" -eq 0 ] && sync && echo 1 > /proc/sys/vm/drop_caches || echo "cache not cleared, needs sudo"

container=kg2pg_container_dbpedia2020

# Remove container if it already exists
echo "Checking for existing container..."
if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
    echo "Removing existing container: ${container}"
    docker rm -f $container
fi

# Create output directory if it doesn't exist
mkdir -p output/DBpedia2020

echo "About to run docker container: ${container}"

docker run -m 32GB -d --name $container -e "JAVA_TOOL_OPTIONS=-Xmx25g" \
	--mount type=bind,source=$(pwd)/data/,target=/app/data \
	--mount type=bind,source=$(pwd)/output/,target=/app/output \
	--mount type=bind,source=$(pwd)/config/,target=/app/config $image \
	/app/config/dbpedia2020.properties
### Logging memory consumption stats by docker container

docker ps

# Get the status of the current docker container
status=$(docker container inspect -f '{{.State.Status}}' $container)

echo "Status of the ${container} is ${status}"

### Monitor container while processing
elapsed=0
while :
do
  status=$(docker container inspect -f '{{.State.Status}}' $container)
  if [ $status == "exited" ]; then
    break
  fi
  docker stats --no-stream | cat >>   "${container}-Docker-Stats.csv"
  echo "⏳ Processing dataset... Elapsed: ${elapsed} min - $(date +%T)"
  sleep 1m
  ((elapsed++))
done

status=$(docker container inspect -f '{{.State.Status}}' $container)

# Check exit code
exit_code=$(docker container inspect -f '{{.State.ExitCode}}' $container)
echo ""
echo "========================================="
echo "Container ${container} has exited"
echo "Exit Code: ${exit_code}"
echo "========================================="
echo ""

# Show container logs
echo "========================================="
echo "Container Logs:"
echo "========================================="
docker logs $container
echo ""

# Validate results
echo "========================================="
echo "VALIDATION RESULTS"
echo "========================================="

if [ ${exit_code} -eq 0 ]; then
    echo "✅ Container completed successfully (Exit Code: 0)"
else
    echo "❌ Container failed (Exit Code: ${exit_code})"
fi

echo ""
echo "Output Directory: $(pwd)/output/DBpedia2020/"
echo ""

if [ -d "output/DBpedia2020" ]; then
    echo "Generated Files:"
    ls -lh output/DBpedia2020/
    echo ""
    
    # Count files in timestamped subdirectory
    if ls output/DBpedia2020/dbpedia_ml_* 1> /dev/null 2>&1; then
        file_count=$(find output/DBpedia2020/dbpedia_ml_* -type f | wc -l | tr -d ' ')
        echo "Total files generated: ${file_count}"
        echo ""
        echo "Key files to check:"
        echo "  - PG_NODES_WD_LABELS.csv (node labels)"
        echo "  - PG_RELATIONS.csv (relationships)"
        echo "  - PG_SCHEMA.txt (schema definition)"
    fi
else
    echo "❌ ERROR: Output directory not found!"
fi

echo ""
echo "========================================="
echo "Next Steps:"
echo "========================================="
echo "1. View output files:"
echo "   ls -lh $(pwd)/output/DBpedia2020/"
echo ""
echo "2. Check node count:"
echo "   wc -l $(pwd)/output/DBpedia2020/*/PG_NODES_WD_LABELS.csv"
echo ""
echo "3. Check relationship count:"
echo "   wc -l $(pwd)/output/DBpedia2020/*/PG_RELATIONS.csv"
echo ""
echo "4. View logs: docker logs ${container}"
echo "5. Remove container: docker rm ${container}"
echo "========================================="
