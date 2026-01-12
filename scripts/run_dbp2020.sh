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

echo ""
echo "========================================="
echo "⚠️  IMPORTANT: Container is running in background"
echo "⚠️  This script will WAIT until processing completes"
echo "⚠️  Do NOT terminate this script prematurely"
echo "========================================="
echo ""
echo "Started at: $(date)"
echo ""

# Disable exit on error for the monitoring loop
set +e

# Monitor container until it stops
elapsed=0
while docker ps -q --filter "name=${container}" --filter "status=running" | grep -q .; do
  # Collect stats (suppress errors)
  docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" ${container} 2>/dev/null | tail -n +2 >> "${container}-Docker-Stats.csv" 2>&1 || true
  
  # Show progress
  echo "⏳ [$(date +%T)] Elapsed: ${elapsed} min - Container is actively processing..."
  
  # Show recent logs every 5 minutes for visibility
  if [ $((elapsed % 5)) -eq 0 ] && [ $elapsed -gt 0 ]; then
    echo "   📋 Recent activity:"
    docker logs --tail 3 $container 2>&1 | sed 's/^/      /' || true
  fi
  
  sleep 1m
  ((elapsed++))
done

# Re-enable exit on error
set -e

echo ""
echo "✅ Container has finished!"
echo "Completed at: $(date)"
echo "Total elapsed time: ${elapsed} minutes"
echo ""

# Get final status and exit code
status=$(docker container inspect -f '{{.State.Status}}' $container)
exit_code=$(docker container inspect -f '{{.State.ExitCode}}' $container)

# Check exit code
exit_code=$(docker container inspect -f '{{.State.ExitCode}}' $container)
echo ""
echo "========================================="
echo "RESULTS"
echo "========================================="

if [ ${exit_code} -eq 0 ]; then
    echo "✅ Container completed successfully (Exit Code: 0)"
else
    echo "❌ Container failed (Exit Code: ${exit_code})"
    echo ""
    echo "Last 10 lines of logs:"
    docker logs --tail 10 $container 2>&1 | sed 's/^/  /'
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
