@echo off
echo ===============================================
echo PayFlow Gateway - Complete Startup Script
echo ===============================================

echo.
echo Step 1: Checking Docker...
docker version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop first and try again.
    pause
    exit /b 1
)

echo Docker is running ✓
echo.

echo Step 2: Starting database services...
docker-compose up -d

if %errorlevel% neq 0 (
    echo ERROR: Failed to start containers!
    pause
    exit /b 1
)

echo.
echo Step 3: Waiting for PostgreSQL to be ready...
timeout /t 15 /nobreak >nul

echo.
echo Step 4: Checking container health...
docker-compose ps

echo.
echo Step 5: Building PayFlow Gateway...
call .\mvnw.cmd clean compile
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Step 6: Starting PayFlow Gateway...
echo Starting application on http://localhost:8080
echo API Documentation: http://localhost:8080/swagger-ui.html
echo Database Admin: http://localhost:8081
echo.

call .\mvnw.cmd spring-boot:run

echo.
pause