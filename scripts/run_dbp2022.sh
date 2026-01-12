#!/bin/bash
set -e
cd ..

### Build Docker Image
image=kg2pg:dockerImage
docker build . -t $image

### Clear Cache
echo "Clearing cache"
[ "$EUID" -eq 0 ] && sync && echo 1 > /proc/sys/vm/drop_caches || echo "cache not cleared, needs sudo"


container=kg2pg_container_dbpedia2022

# Remove container if it already exists
echo "Checking for existing container..."
if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
    echo "Removing existing container: ${container}"
    docker rm -f $container
fi

# Create output directory if it doesn't exist
mkdir -p output/Dbpedia2022

echo "About to run docker container: ${container}"

docker run -m 64GB -d --name $container -e "JAVA_TOOL_OPTIONS=-Xmx45g" \
	--mount type=bind,source=$(pwd)/data/,target=/app/data \
	--mount type=bind,source=$(pwd)/output/,target=/app/output \
	--mount type=bind,source=$(pwd)/config/,target=/app/config $image \
	/app/config/dbpedia2022.properties
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
echo "Checking output directory..."
echo "========================================="
if [ -d "output/Dbpedia2022" ]; then
    echo "Files in output/Dbpedia2022/:"
    ls -lh output/Dbpedia2022/
else
    echo "ERROR: Output directory not found!"
fi
echo ""

echo "To view logs again, run: docker logs ${container}"
echo "To remove the container, run: docker rm ${container}"
