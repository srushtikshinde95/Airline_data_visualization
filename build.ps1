Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "  Building Airline Data Visualization Software" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan

$projectRoot = $PSScriptRoot
$srcDir = Join-Path $projectRoot "src\main\java"
$resDir = Join-Path $projectRoot "src\main\resources"
$binDir = Join-Path $projectRoot "bin"

if (-not (Test-Path $binDir)) {
    New-Item -ItemType Directory -Path $binDir -Force | Out-Null
}

$javaFiles = Get-ChildItem -Path $srcDir -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName
Write-Host "Compiling $($javaFiles.Count) Java source files..." -ForegroundColor Yellow

javac -encoding UTF-8 -d $binDir $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit 1
}

# Copy resource assets
if (Test-Path $resDir) {
    Copy-Item "$resDir\*" $binDir -Recurse -Force
}

Write-Host "[SUCCESS] Build completed successfully!" -ForegroundColor Green
Write-Host "Run the application with: .\run.ps1" -ForegroundColor Cyan