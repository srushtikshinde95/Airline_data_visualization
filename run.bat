@echo off
if not exist bin (
    echo Bin directory not found. Building project...
    call build.bat
)
echo Launching Airline Data Visualization Software...
java -cp bin com.airdata.main.AirlineApp