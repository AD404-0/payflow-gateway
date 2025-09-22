@echo off
echo ===============================================
echo PayFlow Gateway - Database Setup Script
echo ===============================================

echo.
echo Checking if Docker is running...
docker version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running!
    echo.
    echo Please start Docker Desktop first:
    echo 1. Open Docker Desktop from the Start menu
    echo 2. Wait for Docker to start completely
    echo 3. Then run this script again
    echo.
    pause
    exit /b 1
)

echo Docker is running ✓
echo.

echo Starting PostgreSQL and Redis containers...
docker-compose up -d

if %errorlevel% neq 0 (
    echo ERROR: Failed to start containers!
    echo Please check the docker-compose.yml file and try again.
    pause
    exit /b 1
)

echo.
echo Waiting for PostgreSQL to be ready...
timeout /t 10 /nobreak >nul

echo.
echo Checking container status...
docker-compose ps

echo.
echo ===============================================
echo Database setup complete!
echo ===============================================
echo.
echo PostgreSQL Database:
echo   URL: jdbc:postgresql://localhost:5432/payflowdb
echo   Username: payflow_user
echo   Password: payflow_secure_password
echo.
echo Redis Cache:
echo   Host: localhost:6379
echo   Password: payflow_redis_password
echo.
echo PgAdmin (Database Management):
echo   URL: http://localhost:8081
echo   Email: admin@payflow.com
echo   Password: admin123
echo.
echo To stop the database: docker-compose down
echo To view logs: docker-compose logs -f
echo.
pause