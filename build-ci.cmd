@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo Building Trains Service for CI/CD
echo ==========================================

REM Clean
echo Cleaning...
call mvn clean -q
if !errorlevel! neq 0 (
  echo ❌ Clean failed
  exit /b 1
)

REM Compile
echo Compiling...
call mvn compile -q
if !errorlevel! neq 0 (
  echo ❌ Compilation failed
  exit /b 1
)
echo ✅ Compilation successful

REM Test
echo.
echo Running tests...
call mvn test -q
if !errorlevel! neq 0 (
  echo ❌ Tests failed
  exit /b 1
)
echo ✅ All tests passed

REM Package
echo.
echo Packaging...
call mvn package -DskipTests -q
if !errorlevel! neq 0 (
  echo ❌ Package failed
  exit /b 1
)
echo ✅ Package created successfully

echo.
echo ==========================================
echo ✅ BUILD SUCCESSFUL
echo ==========================================
echo.
echo JAR created: target/Trains-Service-*.jar
echo.

endlocal
