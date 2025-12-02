# Loading KG2PG Output into Neo4j

Guide for importing Property Graph data into Neo4j.

## Quick Neo4j Setup with Docker

```bash
# Create directories
mkdir -p neo4j/{data,logs,conf,plugins,import}

# Download plugins
cd neo4j/plugins
wget https://github.com/neo4j/apoc/releases/download/5.11.0/apoc-5.11.0-core.jar
wget https://github.com/neo4j-labs/neosemantics/releases/download/5.7.0.0/neosemantics-5.7.0.0.jar
cd ../..

# Run Neo4j
docker run -d --name neo4j \
  -p 7474:7474 -p 7687:7687 \
  -v $(pwd)/neo4j/data:/data \
  -v $(pwd)/neo4j/logs:/logs \
  -v $(pwd)/neo4j/plugins:/plugins \
  -v $(pwd)/output:/var/lib/neo4j/import \
  --env NEO4J_AUTH=neo4j/password \
  --env NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
  --env NEO4J_apoc_import_file_enabled=true \
  neo4j:5.11.0-enterprise
```

## Import Data

```bash
# Import nodes and relationships
docker exec neo4j neo4j-admin database import full \
  --delimiter="|" \
  --array-delimiter=";" \
  --nodes=import/runningExample/PG_NODES_LITERALS.csv \
  --nodes=import/runningExample/PG_NODES_WD_LABELS.csv \
  --relationships=import/runningExample/PG_RELATIONS.csv \
  neo4j

# Restart Neo4j
docker restart neo4j

# Access Neo4j Browser at http://localhost:7474
```

## Load Key-Value Properties

After importing nodes and relationships, load the JSON properties:

```cypher
// Create index
CREATE INDEX node_range_index_iri FOR (n:Node) ON (n.iri);

// Load properties
CALL apoc.load.json("file:///import/runningExample/PG_NODES_PROPS_JSON.json") 
YIELD value
MATCH (n:Node) WHERE n.iri = value.iri
SET n += value.properties;
```

## Advanced Configuration

For production use with large datasets:

```bash
#!/bin/bash
# run_neo4j.sh - Production Neo4j setup

CONTAINER_NAME=neo4j
NEO4J_VERSION=5.11.0-enterprise

# Define paths
DATA_DIR=$(pwd)/neo4j/data
LOGS_DIR=$(pwd)/neo4j/logs
CONF_DIR=$(pwd)/neo4j/conf
PLUGINS_DIR=$(pwd)/neo4j/plugins
IMPORT_DIR=$(pwd)/output

docker run --detach --name $CONTAINER_NAME \
  --publish=7474:7474 --publish=7687:7687 \
  --volume=$DATA_DIR:/data \
  --volume=$LOGS_DIR:/logs \
  --volume=$CONF_DIR:/conf \
  --volume=$PLUGINS_DIR:/plugins \
  --volume=$IMPORT_DIR:/var/lib/neo4j/import \
  --env NEO4J_dbms_memory_pagecache_size=30G \
  --env NEO4J_server_memory_heap_initial__size=10G \
  --env NEO4J_server_memory_heap_max__size=120G \
  --env NEO4J_dbms.unmanaged_extension_classes=n10s.endpoint=/rdf \
  --env=NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
  --env=NEO4J_apoc_import_file_enabled=true \
  neo4j:$NEO4J_VERSION
```

## Batch Loading JSON Properties

For large datasets, split and load JSON properties in batches:

```bash
#!/bin/bash
# load_json_props.sh - Load JSON properties in batches

json_directory="splitted_json/"
neo4j_user="neo4j"
neo4j_password="password"
neo4j_database="yourdatabase"

for json_file in "${json_directory}"split_file_*.json; do
    file_name=$(basename "$json_file")
    query="CALL apoc.load.json(\"file:///import/dbpedia2022/splitted_json/${file_name}\") 
           YIELD value 
           MATCH (n:Node) WHERE n.iri = value.iri 
           SET n += value.properties;"
    
    echo "Processing $file_name..."
    docker exec neo4j cypher-shell \
      -u "$neo4j_user" -p "$neo4j_password" -d "$neo4j_database" \
      "$query"
done
```

## Running Queries

Sample queries are available in the [resources](https://github.com/dkw-aau/KG2PG/tree/master/src/main/resources) directory.

After loading your Property Graph into Neo4j, you can run analytical queries. Use the benchmark methods in the codebase to test query performance.
