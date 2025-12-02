# Testing KG2PG with Docker

## Quick Start - Running Example Test

This guide shows how to test KG2PG using the small running example dataset.

### Prerequisites

- Docker installed and running
- The project files in `/Users/kashifrabbani/git/KG2PG`

### Step 1: Build the Docker Image

First, build the Docker image (only needed once, or after code changes):

```bash
cd /Users/kashifrabbani/git/KG2PG
./build-docker.sh
```

OR manually:

```bash
docker build . -t kg2pg:dockerImage
```

This will take some time (5-10 minutes or more) as it compiles the Java project.

### Step 2: Run the Test

Once the image is built, run the test:

```bash
cd /Users/kashifrabbani/git/KG2PG/scripts
./test_runningExample.sh
```

### What the Test Does

The test processes:
- **Input Dataset**: `data/runningExampleGraph.nt` (6KB RDF file)
- **SHACL Shapes**: `data/runningExampleShapes.ttl` (11KB shapes file)
- **Configuration**: `config/runningExample.properties`
- **Output Directory**: `output/runningExample/`

Expected outputs:
- `PG_NODES_LITERALS.csv` - Node properties
- `PG_RELATIONS.csv` - Relationships
- `PG_SCHEMA.txt` - Schema information
- `PG_NODES_WD_LABELS.csv` - Node labels
- `PG_PREFIX_MAP.csv` - Prefix mappings
- Additional query files

### Troubleshooting the Reviewer's Issue

The reviewer reported that `run_bio2rdf.sh` produces no output. Key fixes applied:

#### Issue 1: Missing Output Directory Mount
**Problem**: The original script mounted the entire project as `/app/local` but the Java application writes to `/app/output`.

**Old code**:
```bash
--mount type=bind,source=$(pwd),target=/app/local
```

**Fixed**:
```bash
--mount type=bind,source=$(pwd)/output/,target=/app/output \
--mount type=bind,source=$(pwd)/config/,target=/app/config
```

#### Issue 2: Incorrect Config Path
**Problem**: Config path referenced `/app/local/config/...` but should be `/app/config/...`

**Old code**:
```bash
/app/local/config/bio2rdf.properties
```

**Fixed**:
```bash
/app/config/bio2rdf.properties
```

#### Issue 3: No Error Logging
**Problem**: When the container exits, there's no visibility into what went wrong.

**Added**:
- Container exit code checking
- Full log output display
- Output directory verification
- File listing after completion

### Testing the Bio2RDF Fix

To verify the fix for the bio2rdf script:

1. First, ensure you have the bio2rdf data files:
   - `data/bio2rdf.nt` (dataset)
   - `data/dbpedia/bio2rdf.ttl` (shapes)

2. Create the output directory:
   ```bash
   mkdir -p output/bio2rdf
   ```

3. Run the updated script:
   ```bash
   cd /Users/kashifrabbani/git/KG2PG/scripts
   ./run_bio2rdf.sh
   ```

4. The script will now:
   - Show the exit code
   - Display all container logs
   - List the output files
   - Help diagnose any issues

### Files Created for Testing

1. **config/runningExample.properties** - Configuration for running example test
2. **scripts/test_runningExample.sh** - Quick test script (doesn't rebuild Docker)
3. **scripts/run_runningExample.sh** - Full test script (includes Docker build)

### Common Issues

#### "Docker image not found"
Run `./build-docker.sh` or `docker build . -t kg2pg:dockerImage`

#### "Container exits immediately with code 1"
- Check the logs shown by the script
- Verify data files exist: `ls -lh data/runningExample*`
- Verify config file: `cat config/runningExample.properties`

#### "No output generated"
- Check container logs for Java exceptions
- Verify file paths in properties file are correct
- Ensure sufficient memory (container uses 8GB, needs 6GB for Java)

#### "File not found" errors in logs
- Paths in properties file should be relative to `/app/` in the container
- Dataset path: `data/runningExampleGraph.nt`
- Shapes path: `data/runningExampleShapes.ttl`
- Output path: `output/runningExample/`

### Manual Testing

To manually test without the script:

```bash
# Build image
docker build . -t kg2pg:dockerImage

# Run container
docker run -m 8GB --name test_kg2pg \
  --mount type=bind,source=$(pwd)/data/,target=/app/data \
  --mount type=bind,source=$(pwd)/output/,target=/app/output \
  --mount type=bind,source=$(pwd)/config/,target=/app/config \
  kg2pg:dockerImage /app/config/runningExample.properties

# Check logs
docker logs test_kg2pg

# Check output
ls -lh output/runningExample/

# Cleanup
docker rm test_kg2pg
```

### Verification

After a successful run, you should see files in `output/runningExample/`:
```bash
ls -lh output/runningExample/
```

Expected files:
- PG_NODES_LITERALS.csv
- PG_RELATIONS.csv  
- PG_SCHEMA.txt
- PG_NODES_WD_LABELS.csv
- PG_PREFIX_MAP.csv
- Additional output files
