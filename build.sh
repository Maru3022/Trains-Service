#!/bin/bash

# Build and Test Script for Trains Service

set -e

echo "================================"
echo "🔨 Building Trains Service..."
echo "================================"

# Clean previous builds
echo "Cleaning..."
mvn clean

# Compile
echo "Compiling..."
mvn compile

# Run tests
echo "Running tests..."
mvn test -DskipITs=false

# Package
echo "Packaging..."
mvn package -DskipTests

echo ""
echo "================================"
echo "✅ Build successful!"
echo "================================"
echo ""
echo "Test Reports:"
echo "- Unit Tests: target/surefire-reports/"
echo "- Code Coverage: target/site/jacoco/index.html"
echo ""
echo "To run the application:"
echo "  mvn spring-boot:run"
echo ""
echo "To run with test profile:"
echo "  mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=test'"
echo ""
