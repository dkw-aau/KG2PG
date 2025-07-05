#!/bin/bash

echo "Building KG2PG Docker image for multiple platforms..."

# Build for both amd64 and arm64
docker buildx build --platform linux/amd64,linux/arm64 -t kg2pg:latest .

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Docker image built successfully for multiple platforms!"
    echo ""
    echo "Usage examples:"
    echo "  # Run with embedded config and data:"
    echo "  docker run --rm kg2pg:latest"
    echo ""
    echo "  # Run with custom data (mount external directories):"
    echo "  docker run --rm -v \$(pwd)/data:/app/data -v \$(pwd)/output:/app/output kg2pg:latest"
    echo ""
    echo "  # Run with custom config:"
    echo "  docker run --rm -v \$(pwd)/config.properties:/app/config.properties kg2pg:latest"
    echo ""
else
    echo "❌ Docker build failed!"
    exit 1
fi
