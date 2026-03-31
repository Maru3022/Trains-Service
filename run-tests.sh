#!/bin/bash
set -e

echo "🚀 Building Trains Service..."
echo "========================================"

# Step 1: Clean
echo "Step 1: Cleaning..."
mvn clean -q

# Step 2: Compile
echo "Step 2: Compiling..."
mvn compile -q
if [ $? -ne 0 ]; then
  echo "❌ Compilation failed"
  exit 1
fi

# Step 3: Test
echo "Step 3: Running tests..."
mvn test -q
if [ $? -ne 0 ]; then
  echo "❌ Tests failed"
  exit 1
fi

# Step 4: Package
echo "Step 4: Packaging..."
mvn package -DskipTests -q
if [ $? -ne 0 ]; then
  echo "❌ Package failed"
  exit 1
fi

echo ""
echo "========================================"
echo "✅ BUILD SUCCESSFUL!"
echo "========================================"
