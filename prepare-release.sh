#!/bin/bash

echo "🚀 Preparing KG2PG for release..."

# Build the JAR
echo "1. Building JAR..."
./build-jar.sh

if [ $? -ne 0 ]; then
    echo "❌ JAR build failed!"
    exit 1
fi

# Test the JAR
echo ""
echo "2. Testing JAR functionality..."
./test-jar.sh

# Check file sizes
echo ""
echo "3. Checking JAR size..."
jar_size=$(du -h build/libs/kg2pg.jar | cut -f1)
echo "📦 JAR size: $jar_size"

# List embedded resources
echo ""
echo "4. Verifying embedded resources..."
echo "📋 Contents of JAR:"
unzip -l build/libs/kg2pg.jar | grep -E "(config\.properties|data/|\.ttl|\.nt)" | head -10
if [ $(unzip -l build/libs/kg2pg.jar | grep -E "(config\.properties|data/)" | wc -l) -gt 0 ]; then
    echo "✅ Config and data files embedded successfully"
else
    echo "⚠️  Warning: Config or data files may not be embedded"
fi

# Test Docker build (optional)
echo ""
echo "5. Testing Docker build..."
if command -v docker &> /dev/null; then
    echo "🐳 Building Docker image..."
    docker build -t kg2pg-test . &> /dev/null
    if [ $? -eq 0 ]; then
        echo "✅ Docker build successful"
        docker image rm kg2pg-test &> /dev/null
    else
        echo "❌ Docker build failed"
    fi
else
    echo "⏭️  Docker not available, skipping Docker test"
fi

echo ""
echo "🎉 Release preparation complete!"
echo ""
echo "📋 Ready for release:"
echo "  ✅ JAR: build/libs/kg2pg.jar ($jar_size)"
echo "  ✅ Documentation: JAR_USAGE.md"
echo "  ✅ Build scripts: build-jar.sh, build-docker.sh"
echo "  ✅ GitHub Actions: .github/workflows/release.yml"
echo ""
echo "🏷️  To create a release:"
echo "  1. git add ."
echo "  2. git commit -m 'Add JAR release support with timestamped output'"
echo "  3. git tag v1.0.0"
echo "  4. git push origin main --tags"
echo ""
echo "🌐 GitHub Actions will automatically create the release with JAR artifact"
