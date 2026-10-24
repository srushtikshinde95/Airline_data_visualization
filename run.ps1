Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Launching Aircraft Predictive Maintenance System        " -ForegroundColor Cyan
Write-Host "  Web Dashboard: http://localhost:8081                     " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

$projectRoot = $PSScriptRoot
Set-Location $projectRoot

mvn exec:java "-Dexec.mainClass=com.airdata.web.WebAppMain"
