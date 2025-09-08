# Troubleshooting "Failed to fetch" Login Error

## Quick Diagnosis Steps

### 1. Check if Backend Server is Running

**Method 1: Open in Browser**
- Go to: http://localhost:8091/api/auth/test
- **Expected Result**: You should see a JSON response like:
  ```json
  {
    "status": "OK",
    "message": "Backend server is running",
    "timestamp": "2024-01-01T12:00:00Z",
    "server": "Spring Boot",
    "version": "1.0.0"
  }
  ```

**Method 2: Command Line Test**
```bash
curl http://localhost:8091/api/auth/test
```

### 2. Start Backend Server (if not running)

**Option A: Using IDE**
1. Open backend project in your IDE
2. Run `BackendApplication.java` main method

**Option B: Using Command Line**
```bash
cd backend
mvn spring-boot:run
```

**Option C: Using Maven Wrapper**
```bash
cd backend
./mvnw spring-boot:run
```

### 3. Check MySQL Database

Make sure MySQL is running and accessible:

**Database Connection Test:**
```bash
mysql -u root -p
# Enter password: admin
USE artisan;
SHOW TABLES;
```

### 4. Check Common Port Issues

**Check if port 8091 is in use:**
```bash
# Windows
netstat -an | findstr :8091

# Linux/Mac
lsof -i :8091
```

### 5. Frontend Connection Test

**Using Browser Console:**
```javascript
fetch('http://localhost:8091/api/auth/test')
  .then(response => response.json())
  .then(data => console.log('✅ Backend reachable:', data))
  .catch(error => console.error('❌ Backend not reachable:', error));
```

## Common Solutions

### Solution 1: Backend Not Running
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Solution 2: Database Connection Issues
1. Start MySQL service
2. Check database credentials in `application.properties`
3. Ensure database `artisan` exists

### Solution 3: Port Conflicts
1. Change backend port in `application.properties`:
   ```properties
   server.port=8092
   ```
2. Update frontend API URL in auth service

### Solution 4: CORS Issues
If you see CORS errors in browser console:
1. Check `application.properties`:
   ```properties
   app.cors.allowed-origins=http://localhost:4200
   ```
2. Restart backend after changes

### Solution 5: Firewall/Antivirus
- Add exception for ports 4200 and 8091
- Temporarily disable firewall to test

## Step-by-Step Debugging

### Step 1: Verify Services
- [ ] MySQL service is running
- [ ] Backend server starts without errors
- [ ] Frontend development server is running
- [ ] No port conflicts

### Step 2: Test Endpoints
- [ ] http://localhost:8091/api/auth/test returns JSON
- [ ] http://localhost:4200 loads the Angular app
- [ ] Browser console shows no CORS errors

### Step 3: Check Logs
**Backend logs should show:**
```
Started BackendApplication in X seconds
Tomcat started on port(s): 8091 (http)
```

**Frontend logs should show:**
```
Angular Live Development Server is listening on localhost:4200
✓ Compiled successfully
```

## Getting Help

If you're still experiencing issues:

1. **Check browser console** for detailed error messages
2. **Check backend logs** for startup errors
3. **Use the connection test button** in the login form
4. **Verify all services are running** on correct ports

## Success Indicators

✅ **Everything Working:**
- Backend test endpoint returns JSON
- Login form shows no connection errors
- Browser console has no CORS errors
- All services running on expected ports