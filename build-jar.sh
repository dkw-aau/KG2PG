#!/bin/bash

echo "Building KG2PG JAR with embedded resources..."
./gradlew clean shadowJar

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ JAR built successfully!"
    echo "📁 Location: build/libs/kg2pg.jar"
    echo ""
    echo "Usage examples:"
    echo "  # Run with embedded config and data (creates timestamped output):"
    echo "  java -jar build/libs/kg2pg.jar"
    echo "  # Output will be in: output/GraphNpm_2025-07-05_14-30-45_1720188645/"
    echo ""
    echo "  # Run with custom config file:"
    echo "  java -jar build/libs/kg2pg.jar config/custom.properties"
    echo ""
    echo "  # Run with external data (create data/ directory first):"
    echo "  mkdir -p data"
    echo "  # Copy your data files to data/"
    echo "  java -jar build/libs/kg2pg.jar"
    echo "  # Each run creates a new timestamped directory in output/"
    echo ""
else
    echo "❌ Build failed!"
    exit 1
fi
