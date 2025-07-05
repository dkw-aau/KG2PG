#!/bin/bash

echo "Testing KG2PG JAR with timestamped output..."

# Create a clean test environment
mkdir -p test-jar-run
cd test-jar-run

# Copy the JAR to test directory
cp ../build/libs/kg2pg.jar .

echo ""
echo "🧪 Running JAR test (this may take a moment)..."
echo "Expected: New timestamped directory should be created in output/"

# Run the JAR and capture output
java -jar kg2pg.jar 2>&1 | head -20

echo ""
echo "📁 Checking output directory structure..."
if [ -d "output" ]; then
    echo "✅ Output directory created"
    ls -la output/
    
    # Count timestamped directories
    dir_count=$(find output -maxdepth 1 -type d -name "*_*_*" | wc -l)
    if [ $dir_count -gt 0 ]; then
        echo "✅ Found $dir_count timestamped output director(ies)"
        echo "📂 Latest directory contents:"
        latest_dir=$(find output -maxdepth 1 -type d -name "*_*_*" | sort | tail -1)
        if [ -n "$latest_dir" ]; then
            echo "Directory: $latest_dir"
            ls -la "$latest_dir" | head -10
        fi
    else
        echo "❌ No timestamped directories found"
    fi
else
    echo "❌ Output directory not created"
fi

# Cleanup
cd ..
echo ""
echo "🧹 Test completed. Test directory: test-jar-run/"
echo "You can examine the results or clean up with: rm -rf test-jar-run/"
