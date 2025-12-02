#!/bin/bash
set -e
cd ..

### Build Docker Image
image=kg2pg:dockerImage
echo "Building Docker image: ${image}..."
docker build . -t $image

### Clear Cache (optional, requires sudo)
echo "Clearing cache (skipping if not sudo)..."
[ "$EUID" -eq 0 ] && sync && echo 1 > /proc/sys/vm/drop_caches || echo "Cache not cleared (needs sudo, but not required)"

### Container configuration
container=kg2pg_container_runningExample

# Remove container if it already exists
echo "Checking for existing container..."
if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
    echo "Removing existing container: ${container}"
    docker rm -f $container
fi

# Create output directory if it doesn't exist
mkdir -p output/runningExample

echo "About to run docker container: ${container}"
echo "Container will process:"
echo "  - Dataset: data/runningExampleGraph.nt"
echo "  - Shapes: data/runningExampleShapes.ttl"
echo "  - Output: output/runningExample/"
echo ""

# Run the container with proper mounts and memory limits
docker run -m 8GB -d --name $container -e "JAVA_TOOL_OPTIONS=-Xmx6g" \
	--mount type=bind,source=$(pwd)/data/,target=/app/data \
	--mount type=bind,source=$(pwd)/output/,target=/app/output \
	--mount type=bind,source=$(pwd)/config/,target=/app/config $image \
	/app/config/runningExample.properties

### Show running containers
docker ps

# Get the status of the current docker container
status=$(docker container inspect -f '{{.State.Status}}' $container)
echo "Status of ${container} is: ${status}"

### Monitor the container and log stats
stats_file="${container}-Docker-Stats.csv"
echo "Logging stats to: ${stats_file}"

while :
do
  status=$(docker container inspect -f '{{.State.Status}}' $container)
  if [ "$status" == "exited" ]; then
    echo "Container has exited"
    break
  fi
  docker stats --no-stream | cat >> "${stats_file}"
  echo "Container still running... sleeping for 1 minute : $(date +%T)"
  sleep 1m
done

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

# Check if output was generated
echo "========================================="
echo "Checking output directory..."
echo "========================================="
if [ -d "output/runningExample" ]; then
    echo "Files in output/runningExample/:"
    ls -lh output/runningExample/
else
    echo "ERROR: Output directory not found!"
fi
echo ""

# Optionally clean up the container (comment out if you want to keep it for inspection)
echo "To view logs again, run: docker logs ${container}"
echo "To remove the container, run: docker rm ${container}"
