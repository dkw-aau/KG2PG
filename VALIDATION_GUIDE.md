# Validation Guide - Reproducing Paper Results

This guide helps you validate that KG2PG has successfully processed datasets and how to compare results with the paper.

## Quick Validation Checklist

After running any script (e.g., `./run_dbp2020.sh`), verify:

1. ✅ **Exit Code 0** - Container completed successfully
2. ✅ **Output directory exists** - Files created in `output/DatasetName/`
3. ✅ **Required files present** - See list below
4. ✅ **File sizes reasonable** - Not empty or suspiciously small

## Understanding Output Location

**IMPORTANT:** All output goes to lowercase `output/` directory (not `Output/`).

Each run creates a **timestamped subdirectory** to prevent overwriting:

```
output/
  ├── DBpedia2020/
  │   └── dbpedia_ml_2025-12-06_14-35-06_1765031706819/
  │       ├── PG_NODES_WD_LABELS.csv
  │       ├── PG_NODES_LITERALS.csv
  │       ├── PG_RELATIONS.csv
  │       ├── PG_PREFIX_MAP.csv
  │       ├── PG_NODES_PROPS_JSON.json
  │       └── PG_SCHEMA.txt
  ├── bio2rdf/
  ├── dbp22dec/
  └── runningExample/
```

## Required Output Files

Every successful run produces these files:

| File | Description | What to Check |
|------|-------------|---------------|
| `PG_NODES_WD_LABELS.csv` | Node labels and IRIs | Should have rows (1+ per node) |
| `PG_NODES_LITERALS.csv` | Literal property values | May be empty if no literals |
| `PG_RELATIONS.csv` | Edges/relationships | Should have rows (1+ per edge) |
| `PG_PREFIX_MAP.csv` | Namespace prefixes | Should have 2-10 rows typically |
| `PG_NODES_PROPS_JSON.json` | Key-value properties | JSON array of objects |
| `PG_SCHEMA.txt` | Property graph schema | Human-readable schema definition |
| `*_RUNTIME_LOGS.csv` | Performance metrics | Runtime statistics |

## Validation Commands

### Check File Existence

```bash
# Navigate to output directory
cd output/DBpedia2020/

# List all files in timestamped directory
ls -lh dbpedia_ml_*/

# Count total files
find dbpedia_ml_*/ -type f | wc -l
```

### Count Nodes and Edges

```bash
# Count nodes (subtract 1 for header)
wc -l output/DBpedia2020/*/PG_NODES_WD_LABELS.csv

# Count edges (subtract 1 for header)
wc -l output/DBpedia2020/*/PG_RELATIONS.csv

# Count literal nodes
wc -l output/DBpedia2020/*/PG_NODES_LITERALS.csv
```

### Check Schema

```bash
# View schema file
cat output/DBpedia2020/*/PG_SCHEMA.txt

# Count node types in schema
grep "^(" output/DBpedia2020/*/PG_SCHEMA.txt | wc -l

# Count edge types in schema
grep "^CREATE EDGE TYPE" output/DBpedia2020/*/PG_SCHEMA.txt | wc -l
```

### Inspect Sample Data

```bash
# View first 5 nodes
head -5 output/DBpedia2020/*/PG_NODES_WD_LABELS.csv

# View first 5 relationships
head -5 output/DBpedia2020/*/PG_RELATIONS.csv

# Check JSON structure
head -20 output/DBpedia2020/*/PG_NODES_PROPS_JSON.json
```

## Dataset-Specific Validation

### Running Example (Test Dataset)

**Command:** `./test_runningExample.sh`

**Expected Output:**
- **Location:** `output/runningExample/runningExampleGraph_*/`
- **Node count:** ~12-15 nodes
- **Edge count:** ~30-40 relationships
- **Processing time:** < 1 minute
- **Node types:** University, Department, Person, Student, Course, etc.

**Validation:**
```bash
cd output/runningExample/
wc -l runningExampleGraph_*/PG_NODES_WD_LABELS.csv
# Expected: ~12-15 lines (plus header)

wc -l runningExampleGraph_*/PG_RELATIONS.csv
# Expected: ~30-40 lines (plus header)
```

### DBpedia 2020 Dataset

**Command:** `./run_dbp2020.sh`

**Expected Output:**
- **Location:** `output/DBpedia2020/dbpedia_ml_*/`
- **Dataset file:** `data/dbpedia_ml.nt`
- **Shapes file:** `data/dbpedia/dbpedia_2020_QSE_FULL_SHACL.ttl`
- **Processing time:** 10-15 minutes (on 32GB RAM)
- **Memory:** Uses up to 25GB heap

**Validation:**
```bash
cd output/DBpedia2020/
wc -l dbpedia_ml_*/PG_NODES_WD_LABELS.csv
wc -l dbpedia_ml_*/PG_RELATIONS.csv

# Check runtime logs
cat dbpedia_ml_*/DBpedia2020Graph_RUNTIME_LOGS.csv
```

**Paper Reference:**
- Check node/edge counts against paper Table/Figure [specify which one]
- Compare schema complexity metrics
- Verify runtime is comparable (±20% variance acceptable)

### DBpedia 2022 Dataset

**Command:** `./run_dbp2022.sh`

**Expected Output:**
- **Location:** `output/dbp22dec/dbpedia22_*/`
- **Processing time:** 15-30 minutes (on 64GB RAM)
- **Memory:** May require 32-64GB depending on dataset size

**Validation:**
```bash
cd output/dbp22dec/
wc -l dbpedia22_*/PG_NODES_WD_LABELS.csv
wc -l dbpedia22_*/PG_RELATIONS.csv
```

**Paper Reference:**
- Compare with paper results for DBpedia 2022
- Check transformation completeness

### Bio2RDF Clinical Trials

**Command:** `./run_bio2rdf.sh`

**Expected Output:**
- **Location:** `output/bio2rdf/bio2rdf_*/`
- **Processing time:** 5-10 minutes
- **Memory:** Uses up to 6GB heap (8GB RAM sufficient)

**Validation:**
```bash
cd output/bio2rdf/
wc -l bio2rdf_*/PG_NODES_WD_LABELS.csv
wc -l bio2rdf_*/PG_RELATIONS.csv
```

**Paper Reference:**
- Check Bio2RDF results against paper metrics

## Common Issues and Solutions

### Issue 1: "No such file or directory"

**Problem:** You're looking in the wrong directory or using wrong case.

**Solution:**
```bash
# WRONG - capitalized or wrong path
ls Output/DBpedia/          # ❌
ls output/DBpedia/          # ❌

# CORRECT - lowercase, correct dataset name
ls output/DBpedia2020/      # ✅
ls output/bio2rdf/          # ✅
ls output/runningExample/   # ✅
```

### Issue 2: "Directory is empty"

**Problem:** Script failed or output wasn't generated.

**Solution:**
1. Check exit code - should be 0
2. View container logs: `docker logs kg2pg_container_dbpedia2020`
3. Check for errors in logs
4. Verify input files exist in `data/` directory

### Issue 3: "Files are suspiciously small"

**Problem:** Processing may have failed early.

**Solution:**
1. Check `*_RUNTIME_LOGS.csv` for completion status
2. Verify all transformation phases completed in logs
3. Compare file sizes with expected ranges (if provided)

### Issue 4: "Container keeps running"

**Problem:** Large dataset still processing.

**Solution:**
- Monitor the "⏳ Processing dataset... Elapsed: X min" messages
- Check Docker stats: `docker stats kg2pg_container_dbpedia2020`
- Expected times: 1-30 minutes depending on dataset
- If stuck > 1 hour, check logs for errors

## Comparing with Paper Results

### Step 1: Identify Paper Metrics

Locate in the paper:
- Table/Figure with dataset statistics
- Node counts, edge counts, schema complexity
- Runtime performance metrics

### Step 2: Extract Your Results

```bash
# Node count
nodes=$(wc -l output/DBpedia2020/*/PG_NODES_WD_LABELS.csv | awk '{print $1-1}')
echo "Nodes: $nodes"

# Edge count
edges=$(wc -l output/DBpedia2020/*/PG_RELATIONS.csv | awk '{print $1-1}')
echo "Edges: $edges"

# Node types
node_types=$(grep "^(" output/DBpedia2020/*/PG_SCHEMA.txt | wc -l)
echo "Node types: $node_types"

# Edge types
edge_types=$(grep "^CREATE EDGE TYPE" output/DBpedia2020/*/PG_SCHEMA.txt | wc -l)
echo "Edge types: $edge_types"
```

### Step 3: Compare

Acceptable variance:
- ✅ **Exact match** - Perfect reproduction
- ✅ **±5%** - Minor differences (acceptable)
- ⚠️ **±10-20%** - May indicate version differences
- ❌ **>20%** - Investigate potential issues

### Step 4: Check Runtime

```bash
# View runtime breakdown
cat output/DBpedia2020/*/DBpedia2020Graph_RUNTIME_LOGS.csv
```

Compare with paper runtime metrics:
- Different hardware = different times (expected)
- Phase proportions should be similar
- Total time within same order of magnitude

## Getting Help

If validation fails or results don't match:

1. **Check the logs:**
   ```bash
   docker logs kg2pg_container_dbpedia2020 > debug.log
   ```

2. **Verify input data:**
   ```bash
   ls -lh data/
   # Ensure dataset and shapes files exist
   ```

3. **Check configuration:**
   ```bash
   cat config/dbpedia2020.properties
   # Verify paths are correct
   ```

4. **Report issue:**
   - Include exit code, logs, and file counts
   - Mention which dataset and which paper result you're comparing
   - See [GitHub Issues](https://github.com/dkw-aau/KG2PG/issues)

## Summary

**Success criteria:**
1. Exit code 0 ✅
2. Output files exist ✅
3. Files have content (not empty) ✅
4. Counts within expected range ✅
5. Schema makes sense ✅

**Next steps after validation:**
- Load data into Neo4j (see [NEO4J_IMPORT.md](NEO4J_IMPORT.md))
- Run analytical queries
- Compare query results with paper
