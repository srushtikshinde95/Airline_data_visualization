@echo off
echo ==============================================
echo   Building Airline Data Visualization Software
echo ==============================================

if not exist bin mkdir bin

dir /s /b src\main\java\*.java > sources.txt
echo Compiling Java source files...
javac -encoding UTF-8 -d bin @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    del sources.txt
    pause
    exit /b 1
)

del sources.txt
if exist src\main\resources copy src\main\resources\* bin\ >nul

echo [SUCCESS] Build completed successfully!
echo Run the application using run.bat