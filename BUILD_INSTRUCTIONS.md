# KG2PG Build and Run Instructions

## Building the JAR

To create a distributable JAR file with all dependencies and resources embedded:

```bash
./gradlew clean shadowJar
```

This creates `build/libs/kg2pg.jar` - a single file that contains everything needed to run the application.

## Running the JAR

### Option 1: Use embedded defaults (recommended for first run)
```bash
java -jar kg2pg.jar
```

This uses the embedded configuration and sample data. Output will be created in `./output/`.

### Option 2: Use custom external config
```bash
# Create your own config.properties in the same directory as the JAR
java -jar kg2pg.jar config.properties
```

### Option 3: Use completely external setup
```bash
# Create your data and config directories
mkdir -p data output
# Copy your data files to ./data/
# Create custom config.properties with your paths
java -jar kg2pg.jar config.properties
```

## Directory Structure

When running from JAR, the application expects this structure:

```
your-working-directory/
├── kg2pg.jar
├── config.properties (optional - external config)
├── data/ (optional - external data)
│   ├── your-dataset.nt
│   └── your-shapes.ttl
└── output/ (created automatically)
```

## Docker Usage

### Build for multiple architectures (including Apple Silicon):
```bash
docker buildx build --platform linux/amd64,linux/arm64 -t kg2pg:latest .
```

### Run with Docker:
```bash
# Create directories for data and output
mkdir -p data output

# Run the container
docker run -v $(pwd)/data:/app/data -v $(pwd)/output:/app/output kg2pg:latest
```

## Configuration

All paths in config files are now relative to the working directory where you run the JAR:

- `data/` - Input data directory
- `output/` - Output directory (created automatically)
- Config files use relative paths by default

The `resources_path` configuration is no longer needed as resources are embedded in the JAR.

## Backward Compatibility

- Old absolute paths in config files still work
- If `resources_path` is specified, it will be used as a fallback
- Command line arguments work the same way as before

## Troubleshooting

- Make sure you have Java 17 or later installed
- If you get "resource not found" errors, check your data paths in the config file
- Output directory will be created automatically if it doesn't exist
