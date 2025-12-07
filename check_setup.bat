

# ============================================================
# Windows Version (check-setup.bat)
# ============================================================
# Save this as check-setup.bat for Windows:
#
# @echo off
# setlocal enabledelayedexpansion
#
# echo ============================================================
# echo       Smart Energy System - Installation Checker
# echo ============================================================
# echo.
#
# set ERRORS=0
# set WARNINGS=0
#
# echo Checking Prerequisites...
# echo ------------------------
#
# where java >nul 2>&1
# if errorlevel 1 (
#     echo   [X] Java JDK NOT FOUND
#     set /a ERRORS+=1
# ) else (
#     echo   [OK] Java JDK
#     java -version 2>&1 | findstr "version"
# )
#
# where mvn >nul 2>&1
# if errorlevel 1 (
#     echo   [X] Maven NOT FOUND
#     set /a ERRORS+=1
# ) else (
#     echo   [OK] Maven
# )
#
# where python >nul 2>&1
# if errorlevel 1 (
#     echo   [!] Python not found (optional)
# ) else (
#     echo   [OK] Python
# )
#
# echo.
# echo Checking Project Structure...
# echo -----------------------------
#
# if exist "pom.xml" (
#     echo   [OK] pom.xml
# ) else (
#     echo   [X] pom.xml MISSING
#     set /a ERRORS+=1
# )
#
# if exist "src\main\java\web\WebServer.java" (
#     echo   [OK] WebServer.java
# ) else (
#     echo   [X] WebServer.java MISSING
#     set /a ERRORS+=1
# )
#
# if exist "web\dashboard.html" (
#     echo   [OK] dashboard.html
# ) else (
#     echo   [X] dashboard.html MISSING
#     set /a ERRORS+=1
# )
#
# if exist "target\classes" (
#     echo   [OK] Project compiled
# ) else (
#     echo   [!] Project not compiled
#     echo       Run: mvn clean compile
#     set /a WARNINGS+=1
# )
#
# echo.
# echo ============================================================
#
# if %ERRORS% EQU 0 (
#     if %WARNINGS% EQU 0 (
#         echo   PERFECT! Everything looks good
#         echo.
#         echo Ready to start:
#         echo   mvn exec:java -Dexec.mainClass="web.WebServer"
#         echo   or double-click run.bat
#     ) else (
#         echo   Ready with %WARNINGS% warnings
#     )
# ) else (
#     echo   Issues found: %ERRORS% errors, %WARNINGS% warnings
#     echo   Please fix errors before starting
# )
#
# echo.
# pause