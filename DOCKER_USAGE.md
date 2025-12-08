# Docker Usage Guide

Complete guide for running KG2PG with Docker.

## Prerequisites
- Docker installed and running
- Downloaded datasets (if using large datasets)

## Build the Docker Image

```bash
# Build the image (only needed once)
docker build -t kg2pg:dockerImage .
```

This takes 5-10 minutes as it compiles the Java project.

## Quick Test with Sample Data

Test with the included running example:

```bash
cd scripts
./test_runningExample.sh
```

**What it does:**
- Processes `data/runningExampleGraph.nt` (6KB dataset)
- Uses `data/runningExampleShapes.ttl` (SHACL shapes)
- Outputs to `output/runningExample/`
- Shows logs and verifies output

## Running with Large Datasets

After downloading datasets, use the appropriate script:

### Bio2RDF Clinical Trials

```bash
# 1. Download the Bio2RDF dataset to data/ directory
# 2. Update config/bio2rdf.properties if needed
# 3. Run:
cd scripts
./run_bio2rdf.sh
```

**Expected output:** Files in `output/bio2rdf/`

**Validation:**
```bash
# Check results (note: lowercase 'output')
ls -lh output/bio2rdf/

# Count nodes
wc -l output/bio2rdf/*/PG_NODES_WD_LABELS.csv
```

See [VALIDATION_GUIDE.md](VALIDATION_GUIDE.md) for complete validation instructions.

### DBpedia 2020

```bash
# 1. Download DBpedia 2020 dataset to data/ directory
# 2. Ensure data/dbpedia/dbpedia_2020_QSE_FULL_SHACL.ttl exists
# 3. Run:
cd scripts
./run_dbp2020.sh
```

**Expected output:** Files in `output/DBpedia2020/`

**Validation:**
```bash
# Check results (note: lowercase 'output', correct name 'DBpedia2020')
ls -lh output/DBpedia2020/

# Count nodes and edges
wc -l output/DBpedia2020/*/PG_NODES_WD_LABELS.csv
wc -l output/DBpedia2020/*/PG_RELATIONS.csv
```

See [VALIDATION_GUIDE.md](VALIDATION_GUIDE.md) for complete validation instructions.

### DBpedia 2022

```bash
# 1. Download DBpedia 2022 dataset to data/ directory
# 2. Ensure data/dbpedia/DBpedia2022_QSE_SHACL.ttl exists
# 3. Run:
cd scripts
./run_dbp2022.sh
```

**Expected output:** Files in `output/dbp22dec/`

**Validation:**
```bash
# Check results
ls -lh output/dbp22dec/

# Count nodes
wc -l output/dbp22dec/*/PG_NODES_WD_LABELS.csv
```

See [VALIDATION_GUIDE.md](VALIDATION_GUIDE.md) for complete validation instructions.

## Available Scripts

All scripts are in the `scripts/` directory:
- `test_runningExample.sh` - Quick test with sample data
- `run_bio2rdf.sh` - Bio2RDF Clinical Trials dataset
- `run_dbp2020.sh` - DBpedia 2020 dataset  
- `run_dbp2022.sh` - DBpedia 2022 dataset
- `run_dbp2022march.sh` - DBpedia 2022 March version
- `run_dbp2022delta.sh` - DBpedia 2022 delta processing

## Configuration Files

Each dataset has a corresponding configuration file in `config/`:
- `runningExample.properties` - Sample data configuration
- `bio2rdf.properties` - Bio2RDF configuration
- `dbpedia2020.properties` - DBpedia 2020 configuration
- `dbpedia2022.properties` - DBpedia 2022 configuration
- `dbpedia2022march.properties` - DBpedia 2022 March configuration
- `dbpedia2022delta.properties` - DBpedia 2022 delta configuration

### Configuration Format

```properties
dataset_name=YourDatasetName
shapes_path=data/subfolder/shapes.ttl
dataset_path=data/dataset.nt
output_file_path=output/youroutput/
expected_number_classes=50
expected_number_of_lines=1000
is_wikidata=false
```

## Docker Script Features

All Docker scripts include:
- ✅ **Automatic container cleanup** - Removes old containers
- ✅ **Exit code reporting** - Shows if processing succeeded
- ✅ **Full log output** - Displays container logs for debugging
- ✅ **Output verification** - Lists generated files
- ✅ **Memory monitoring** - Tracks Docker stats during execution

## Troubleshooting

### Output Directory Not Found

**Problem:** `ls: cannot access 'Output/DBpedia/': No such file or directory`

**Solution:** Use lowercase `output/` and correct dataset name:

```bash
# WRONG - capitalized or incorrect name
ls Output/DBpedia/          # ❌
ls output/DBpedia/          # ❌

# CORRECT - lowercase, correct dataset name  
ls output/DBpedia2020/      # ✅ for DBpedia 2020
ls output/bio2rdf/          # ✅ for Bio2RDF
ls output/dbp22dec/         # ✅ for DBpedia 2022
ls output/runningExample/   # ✅ for test dataset
```

The script output shows the exact path. Look for:
```
Output Directory: /full/path/to/output/DBpedia2020/
```

### Container Exits with Errors

```bash
# View container logs
docker logs kg2pg_container_runningExample

# Check if output was generated
ls -lh output/runningExample/

# Remove container and try again
docker rm kg2pg_container_runningExample
cd scripts
./test_runningExample.sh
```

### Common Issues

- **Missing data files**: Verify dataset and shapes files exist in `data/`
- **Insufficient memory**: Large datasets need 32-64GB RAM
- **Path errors**: All paths in properties files should be relative (e.g., `data/file.nt`, not `/app/data/file.nt`)

See [DOCKER_TESTING.md](DOCKER_TESTING.md) for detailed testing guide.
