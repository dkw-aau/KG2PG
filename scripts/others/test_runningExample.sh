#!/bin/bash
# Quick test script - assumes Docker image is already built
# Run build-docker.sh first if image doesn't exist

set -e
cd "$(dirname "$0")/.."

container=kg2pg_container_runningExample
image=kg2pg:dockerImage

# Check if image exists
if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^${image}$"; then
    echo "ERROR: Docker image '${image}' not found!"
    echo "Please run './build-docker.sh' first to build the image."
    exit 1
fi

# Remove container if it already exists
if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
    echo "Removing existing container: ${container}"
    docker rm -f $container
fi

# Create output directory if it doesn't exist
mkdir -p output/runningExample

echo "========================================="
echo "Running KG2PG Test with Running Example"
echo "========================================="
echo "Dataset: data/runningExampleGraph.nt"
echo "Shapes:  data/runningExampleShapes.ttl"
echo "Output:  output/runningExample/"
echo ""

# Run the container
docker run -m 8GB -d --name $container -e "JAVA_TOOL_OPTIONS=-Xmx6g" \
	--mount type=bind,source=$(pwd)/data/,target=/app/data \
	--mount type=bind,source=$(pwd)/output/,target=/app/output \
	--mount type=bind,source=$(pwd)/config/,target=/app/config $image \
	/app/config/runningExample.properties

echo "Container started: ${container}"
echo ""

# Monitor the container
echo "Monitoring container (this may take a while)..."
while :
do
  status=$(docker container inspect -f '{{.State.Status}}' $container 2>/dev/null || echo "not-found")
  
  if [ "$status" == "not-found" ]; then
    echo "ERROR: Container disappeared!"
    exit 1
  fi
  
  if [ "$status" == "exited" ]; then
    echo "Container has exited"
    break
  fi
  
  echo "Status: ${status} - $(date +%T)"
  sleep 10
done

# Get exit code
exit_code=$(docker container inspect -f '{{.State.ExitCode}}' $container)
echo ""
echo "========================================="
echo "Container Exit Code: ${exit_code}"
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
echo "Output Files:"
echo "========================================="
if [ -d "output/runningExample" ]; then
    ls -lh output/runningExample/
    echo ""
    echo "Total files: $(find output/runningExample -type f | wc -l)"
else
    echo "ERROR: Output directory not found!"
fi
echo ""

if [ $exit_code -eq 0 ]; then
    echo "✓ SUCCESS: Container completed successfully!"
else
    echo "✗ FAILED: Container exited with code ${exit_code}"
    echo ""
    echo "Troubleshooting tips:"
    echo "  - Check the logs above for error messages"
    echo "  - Verify data files exist: data/runningExampleGraph.nt and data/runningExampleShapes.ttl"
    echo "  - Check config file: config/runningExample.properties"
fi
echo ""
echo "To view logs again: docker logs ${container}"
echo "To remove container: docker rm ${container}"
