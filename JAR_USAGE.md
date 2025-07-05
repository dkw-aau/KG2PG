# KG2PG JAR Release Usage

## Quick Start with JAR

### Building the JAR
```bash
# Build the JAR with all embedded resources
./build-jar.sh
```

This creates `build/libs/kg2pg.jar` with all dependencies and resources embedded.

### Running the JAR

#### Option 1: Run with all defaults (embedded config and data)
```bash
java -jar build/libs/kg2pg.jar
```
Uses embedded sample data and configuration. Each run creates a timestamped directory in `output/`:
- Format: `output/datasetName_YYYY-MM-DD_HH-MM-SS_timestamp/`
- Example: `output/GraphNpm_2025-07-05_14-30-45_1720188645/`

#### Option 2: Run with custom config file
```bash
java -jar build/libs/kg2pg.jar config/custom.properties
```

#### Option 3: Run with external data directories
```bash
# Create directories and add your data
mkdir -p data output
# Copy your RDF files to data/
java -jar build/libs/kg2pg.jar
```

#### Option 4: Run with external config.properties
```bash
# Create config.properties in same directory as JAR
# Customize paths as needed
java -jar build/libs/kg2pg.jar
```

### Configuration Priority
1. Command line argument: `java -jar kg2pg.jar /path/to/config.properties`
2. External `config.properties` in current directory
3. Embedded `config.properties` in JAR

### Data File Priority
1. External files in current working directory
2. Embedded resources in JAR
3. Temporary extraction of embedded resources (when file path required)

## Docker Usage

### Building Multi-Platform Docker Image
```bash
# Build for both Intel and ARM64 (M1/M2 Macs)
./build-docker.sh
```

### Running with Docker
```bash
# Run with embedded defaults
docker run --rm kg2pg:latest

# Run with external data
docker run --rm -v $(pwd)/data:/app/data -v $(pwd)/output:/app/output kg2pg:latest

# Run with custom config
docker run --rm -v $(pwd)/config.properties:/app/config.properties kg2pg:latest
```

## Directory Structure for External Data
```
kg2pg/
├── kg2pg.jar
├── config.properties (optional - for customization)
├── data/ (optional - your RDF data)
│   ├── your-graph.nt
│   ├── your-shapes.ttl
│   └── ...
└── output/ (created automatically)
    ├── YourDataset_2025-07-05_14-30-45_1720188645/
    │   ├── PG_NODES_WD_LABELS.csv
    │   ├── PG_RELATIONS.csv
    │   └── ...
    ├── YourDataset_2025-07-05_15-45-30_1720193130/
    │   ├── PG_NODES_WD_LABELS.csv
    │   ├── PG_RELATIONS.csv
    │   └── ...
    └── ...
```

## Configuration Examples

### Basic config.properties
```properties
dataset_name=MyDataset
shapes_path=data/my-shapes.ttl
dataset_path=data/my-graph.nt
output_file_path=output/
expected_number_classes=100
expected_number_of_lines=10000
is_wikidata=false
```

### Using absolute paths
```properties
dataset_name=MyDataset
shapes_path=/absolute/path/to/shapes.ttl
dataset_path=/absolute/path/to/graph.nt
output_file_path=/absolute/path/to/output/
```

## Benefits of JAR Release
- ✅ Single file distribution - no build required
- ✅ All dependencies embedded
- ✅ Works out of the box with sample data
- ✅ Easy to customize with external files
- ✅ Cross-platform compatibility
- ✅ Docker support for both Intel and ARM64

## Timestamped Output Directories

Each run automatically creates a new timestamped output directory to prevent overwriting previous results:

### Directory Naming Convention
- Format: `{datasetName}_{YYYY-MM-DD}_{HH-MM-SS}_{unix-timestamp}/`
- Example: `GraphNpm_2025-07-05_14-30-45_1720188645/`

### Benefits
- ✅ No output file conflicts between runs
- ✅ Easy to track and compare different runs
- ✅ Chronological organization of results
- ✅ Unique directory names prevent race conditions

### Output Location
- Base directory specified in `output_file_path` config
- Default: `output/` (relative to working directory)
- Custom: Set `output_file_path=/path/to/your/output/` in config
