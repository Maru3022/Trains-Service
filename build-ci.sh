#!/bin/bash
set -e

echo "=========================================="
echo "Building Trains Service for CI/CD"
echo "=========================================="

# Clean
echo "Cleaning..."
mvn clean -q

# Compile
echo "Compiling..."
mvn compile -q
if [ $? -ne 0 ]; then
  echo "❌ Compilation failed"
  exit 1
fi
echo "✅ Compilation successful"

# Test
echo ""
echo "Running tests..."
mvn test -q
if [ $? -ne 0 ]; then
  echo "❌ Tests failed"
  exit 1
fi
echo "✅ All tests passed"

# Package
echo ""
echo "Packaging..."
mvn package -DskipTests -q
if [ $? -ne 0 ]; then
  echo "❌ Package failed"
  exit 1
fi
echo "✅ Package created successfully"

echo ""
echo "=========================================="
echo "✅ BUILD SUCCESSFUL"
echo "=========================================="
echo ""
echo "JAR created: target/Trains-Service-*.jar"
echo ""
