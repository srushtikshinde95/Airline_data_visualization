Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Launching Airline Predictive Maintenance Desktop (Swing) " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$projectRoot = $PSScriptRoot
Set-Location $projectRoot

mvn exec:java "-Dexec.mainClass=com.airdata.Main"
