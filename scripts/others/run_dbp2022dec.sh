#!/bin/bash
set -e
cd ..

### Build Docker Image
image=kg2pg:dockerImage
docker build . -t $image

### Clear Cache
echo "Clearing cache"
[ "$EUID" -eq 0 ] && sync && echo 1 > /proc/sys/vm/drop_caches || echo "cache not cleared, needs sudo"


container=kg2pg_dbpedia2022dec

# Remove container if it already exists
echo "Checking for existing container..."
if docker ps -a --format '{{.Names}}' | grep -q "^${container}$"; then
    echo "Removing existing container: ${container}"
    docker rm -f $container
fi

# Create output directory if it doesn't exist
mkdir -p output/dbp22dec

echo "About to run docker container: ${container}"

docker run -m 64GB -d --name $container -e "JAVA_TOOL_OPTIONS=-Xmx45g" \
	--mount type=bind,source=$(pwd)/data/,target=/app/data \
	--mount type=bind,source=$(pwd)/output/,target=/app/output \
	--mount type=bind,source=$(pwd)/config/,target=/app/config $image \
	/app/config/dbpedia2022.properties
### Logging memory consumption stats by docker container

docker ps

# Get the status of the current docker container
status=$(docker container inspect -f '{{.State.Status}}' $container)

echo "Status of the ${container} is ${status}"

### Keep it in sleep for 1 minutes while this container is running
while :
do
  status=$(docker container inspect -f '{{.State.Status}}' $container)
  if [ $status == "exited" ]; then
    break
  fi
  docker stats --no-stream | cat >>   "${container}-Docker-Stats.csv"
  echo "Sleeping for 1 minutes : $(date +%T)"
  sleep 1m
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

# Check if output was generated
echo "========================================="
echo "Checking output directory..."
echo "========================================="
if [ -d "output/dbp22dec" ]; then
    echo "Files in output/dbp22dec/:"
    ls -lh output/dbp22dec/
else
    echo "ERROR: Output directory not found!"
fi
echo ""

echo "To view logs again, run: docker logs ${container}"
echo "To remove the container, run: docker rm ${container}"
