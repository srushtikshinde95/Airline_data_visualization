$projectRoot = $PSScriptRoot
$binDir = Join-Path $projectRoot "bin"

if (-not (Test-Path $binDir)) {
    Write-Host "Project not compiled yet. Running build first..." -ForegroundColor Yellow
    & "$projectRoot\build.ps1"
}

Write-Host "Launching Airline Data Visualization Software..." -ForegroundColor Green
java -cp "$binDir" com.airdata.main.AirlineApp