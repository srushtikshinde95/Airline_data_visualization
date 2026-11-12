Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Building Aircraft Predictive Maintenance System (Maven)  " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$projectRoot = $PSScriptRoot
Set-Location $projectRoot

mvn clean package
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Maven build failed." -ForegroundColor Red
    exit 1
}

Write-Host "[SUCCESS] Build completed successfully!" -ForegroundColor Green
Write-Host "Run the application with: .\run.ps1" -ForegroundColor Cyan
