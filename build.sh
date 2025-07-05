#!/bin/bash

# Build script for creating a release JAR

echo "Building KG2PG JAR..."
./gradlew clean shadowJar

if [ $? -eq 0 ]; then
    echo "✅ JAR created successfully at: build/libs/kg2pg.jar"
    echo ""
    echo "Usage examples:"
    echo "  java -jar build/libs/kg2pg.jar                    # Use embedded config and data"
    echo "  java -jar build/libs/kg2pg.jar config.properties  # Use external config"
    echo ""
    echo "To create a release, copy build/libs/kg2pg.jar to your desired location."
else
    echo "❌ Build failed"
    exit 1
fi
