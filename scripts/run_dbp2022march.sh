#!/bin/bash
set -e

cd ..

### Build Docker Image
image=kg2pg:dockerImage
docker build . -t $image

### Clear Cache
echo "Clearing cache"
[ "$EUID" -eq 0 ] && sync && echo 1 > /proc/sys/vm/drop_caches || echo "cache not cleared, needs sudo"


container=kg2pg_dbpedia2022marchNPM

echo "About to run docker container: ${container}"

docker run -m 64GB -d --name $container -e "JAVA_TOOL_OPTIONS=-Xmx45g" \
	--mount type=bind,source=$(pwd)/data/,target=/app/data \
	--mount type=bind,source=$(pwd),target=/app/local $image \
	/app/local/config/dbpedia2022march.properties
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

echo "Status of the ${container} is ${status}" ### Container exited
