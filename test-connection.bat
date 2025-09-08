@echo off
REM Artisan Platform - Quick Connection Test Script (Windows)
REM This script helps diagnose the "Failed to fetch" login error

echo.
echo 🔍 Artisan Platform Connection Diagnostics
echo ===========================================
echo.

REM Test 1: Check if backend is reachable
echo Test 1: Backend Connection Test
echo Checking http://localhost:8091/api/auth/test...

curl -s -f http://localhost:8091/api/auth/test >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Backend is reachable!
    echo Response:
    curl -s http://localhost:8091/api/auth/test
) else (
    echo ❌ Backend is NOT reachable
    echo    This is likely the cause of your 'Failed to fetch' error
)

echo.

REM Test 2: Check if ports are listening
echo Test 2: Port Status Check
echo Checking if required ports are open...

netstat -an | findstr ":8091" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Port 8091 (Backend) is listening
) else (
    echo ❌ Port 8091 (Backend) is NOT listening
    echo    You need to start the backend server
)

netstat -an | findstr ":4200" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Port 4200 (Frontend) is listening
) else (
    echo ⚠️  Port 4200 (Frontend) is NOT listening
    echo    You may need to start the Angular dev server
)

echo.

REM Test 3: Check if Java is available
echo Test 3: Java Environment Check
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Java is installed and available
) else (
    echo ❌ Java is NOT available
    echo    You need Java to run the Spring Boot backend
)

echo.

REM Test 4: Check if Node.js is available
echo Test 4: Node.js Environment Check
node --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Node.js is installed and available
) else (
    echo ❌ Node.js is NOT available
    echo    You need Node.js to run the Angular frontend
)

echo.

REM Recommendations
echo 🔧 Quick Fixes:
echo ===============
echo.

curl -s -f http://localhost:8091/api/auth/test >nul 2>&1
if not %errorlevel% equ 0 (
    echo Backend Server Not Running:
    echo   1. cd backend
    echo   2. mvn spring-boot:run
    echo   3. Wait for 'Started BackendApplication' message
    echo.
)

echo Common Startup Commands:
echo   Backend:  cd backend ^&^& mvn spring-boot:run
echo   Frontend: cd frontend ^&^& ng serve
echo   MySQL:    net start mysql
echo.

echo Test URLs:
echo   Backend Health: http://localhost:8091/api/auth/test
echo   Frontend App:   http://localhost:4200
echo.

echo For detailed troubleshooting, see: TROUBLESHOOTING.md
echo.
pause