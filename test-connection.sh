#!/bin/bash

# Artisan Platform - Quick Connection Test Script
# This script helps diagnose the "Failed to fetch" login error

echo "🔍 Artisan Platform Connection Diagnostics"
echo "==========================================="
echo ""

# Test 1: Check if backend is reachable
echo "Test 1: Backend Connection Test"
echo "Checking http://localhost:8091/api/auth/test..."

if curl -s -f http://localhost:8091/api/auth/test > /dev/null 2>&1; then
    echo "✅ Backend is reachable!"
    echo "Response:"
    curl -s http://localhost:8091/api/auth/test | python -m json.tool 2>/dev/null || curl -s http://localhost:8091/api/auth/test
else
    echo "❌ Backend is NOT reachable"
    echo "   This is likely the cause of your 'Failed to fetch' error"
fi

echo ""

# Test 2: Check if ports are listening
echo "Test 2: Port Status Check"
echo "Checking if required ports are open..."

if command -v netstat >/dev/null 2>&1; then
    if netstat -an | grep -q ":8091.*LISTEN" 2>/dev/null; then
        echo "✅ Port 8091 (Backend) is listening"
    else
        echo "❌ Port 8091 (Backend) is NOT listening"
        echo "   You need to start the backend server"
    fi
    
    if netstat -an | grep -q ":4200.*LISTEN" 2>/dev/null; then
        echo "✅ Port 4200 (Frontend) is listening"
    else
        echo "⚠️  Port 4200 (Frontend) is NOT listening"
        echo "   You may need to start the Angular dev server"
    fi
else
    echo "⚠️  netstat command not available, skipping port check"
fi

echo ""

# Test 3: Check MySQL connection (if mysql command is available)
echo "Test 3: Database Connection Test"
if command -v mysql >/dev/null 2>&1; then
    echo "Checking MySQL connection..."
    if mysql -u root -padmin -e "USE artisan; SELECT 1;" >/dev/null 2>&1; then
        echo "✅ MySQL database 'artisan' is accessible"
    else
        echo "❌ MySQL database connection failed"
        echo "   Check if MySQL is running and credentials are correct"
    fi
else
    echo "⚠️  MySQL command not available, skipping database test"
fi

echo ""

# Recommendations
echo "🔧 Quick Fixes:"
echo "==============="
echo ""

if ! curl -s -f http://localhost:8091/api/auth/test > /dev/null 2>&1; then
    echo "Backend Server Not Running:"
    echo "  1. cd backend"
    echo "  2. mvn spring-boot:run"
    echo "  3. Wait for 'Started BackendApplication' message"
    echo ""
fi

echo "Common Startup Commands:"
echo "  Backend:  cd backend && mvn spring-boot:run"
echo "  Frontend: cd frontend && ng serve"
echo "  MySQL:    sudo service mysql start  (Linux/Mac)"
echo "           net start mysql           (Windows)"
echo ""

echo "Test URLs:"
echo "  Backend Health: http://localhost:8091/api/auth/test"
echo "  Frontend App:   http://localhost:4200"
echo ""

echo "For detailed troubleshooting, see: TROUBLESHOOTING.md"