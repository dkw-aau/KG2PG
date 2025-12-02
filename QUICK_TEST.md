# Quick Test Instructions

## Current Status
Docker image is being built... This takes 5-10 minutes.

## Once Build Completes

Run the test:
```bash
cd /Users/kashifrabbani/git/KG2PG/scripts
./test_runningExample.sh
```

## What to Expect

The script will:
1. Check if Docker image exists  
2. Run KG2PG with the running example dataset
3. Show live progress
4. Display logs if there are errors
5. List output files when done

## Test Files

- **Config**: `config/runningExample.properties`
- **Dataset**: `data/runningExampleGraph.nt` (6KB)
- **Shapes**: `data/runningExampleShapes.ttl` (11KB)
- **Output**: `output/runningExample/`

## Expected Output Files

- PG_NODES_LITERALS.csv
- PG_RELATIONS.csv
- PG_SCHEMA.txt
- PG_NODES_WD_LABELS.csv
- PG_PREFIX_MAP.csv

## If There Are Issues

Check logs with:
```bash
docker logs kg2pg_container_runningExample
```

See full documentation in `DOCKER_TESTING.md`
