@echo off
REM Build and Test Script for Trains Service (Windows)

setlocal enabledelayedexpansion

echo ================================
echo 🔨 Building Trains Service...
echo ================================

REM Clean previous builds
echo Cleaning...
call mvn clean
if !errorlevel! neq 0 goto error

REM Compile
echo Compiling...
call mvn compile
if !errorlevel! neq 0 goto error

REM Run tests
echo Running tests...
call mvn test -DskipITs=false
if !errorlevel! neq 0 goto error

REM Package
echo Packaging...
call mvn package -DskipTests
if !errorlevel! neq 0 goto error

echo.
echo ================================
echo ✅ Build successful!
echo ================================
echo.
echo Test Reports:
echo - Unit Tests: target\surefire-reports\
echo - Code Coverage: target\site\jacoco\index.html
echo.
echo To run the application:
echo   mvn spring-boot:run
echo.
echo To run with test profile:
echo   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"
echo.

goto :eof

:error
echo ❌ Build failed!
exit /b 1
