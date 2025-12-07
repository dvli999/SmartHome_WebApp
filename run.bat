@echo off
title Smart Energy System
color 0A
cls

echo ============================================================
echo.
echo          Smart Energy System
echo          One-Click Launcher
echo.
echo ============================================================
echo.

REM Change to script directory
cd /d "%~dp0"

REM Check if Maven is installed
where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven not found!
    echo.
    echo Please install Apache Maven and add it to your PATH.
    echo Download from: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found!
    echo.
    echo Please install Java JDK 8 and add it to your PATH.
    echo Download from: https://www.oracle.com/java/technologies/javase-jdk8-downloads.html
    echo.
    pause
    exit /b 1
)

echo [OK] Prerequisites check passed
echo.
echo Starting Smart Energy System...
echo.
echo This will:
echo   - Start CORBA Name Service
echo   - Start CORBA Server
echo   - Start RMI Server
echo   - Start Web Dashboard
echo   - Open browser automatically
echo.
echo ============================================================
echo.

REM Run the system
mvn exec:java -Dexec.mainClass="web.WebServer"

REM If there was an error
if errorlevel 1 (
    echo.
    echo ============================================================
    echo   ERROR: Failed to start system
    echo ============================================================
    echo.
    echo Possible issues:
    echo   1. Project not built - try running: mvn clean compile
    echo   2. Port already in use - close other instances
    echo   3. Missing dependencies - check pom.xml
    echo.
    echo To manually build the project:
    echo   mvn clean compile
    echo.
    pause
    exit /b 1
)

REM If we get here, the program was closed normally
echo.
echo ============================================================
echo   System stopped successfully
echo ============================================================
echo.
pause