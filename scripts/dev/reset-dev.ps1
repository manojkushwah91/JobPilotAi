param([switch]$Force)

$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

if (-not $Force) {
    $confirmation = Read-Host "This will DELETE ALL DATA and reset the development environment. Continue? (y/N)"
    if ($confirmation -ne 'y') { exit }
}

Write-Host "=== Resetting Development Environment ===" -ForegroundColor Cyan

# Stop everything
& "$RootDir\scripts\dev\stop-dev.ps1"

# Remove Docker volumes
Write-Host "Removing Docker volumes..." -ForegroundColor Yellow
docker compose -f "$RootDir\infrastructure\docker\dev\docker-compose.yml" down -v

# Clean Maven
Write-Host "Cleaning Maven build..." -ForegroundColor Yellow
& "$RootDir\backend\mvnw.cmd" clean -f "$RootDir\backend\pom.xml" 2>$null

# Remove node_modules
Write-Host "Removing node_modules..." -ForegroundColor Yellow
if (Test-Path "$RootDir\frontend\node_modules") {
    Remove-Item -Recurse -Force "$RootDir\frontend\node_modules"
}

Write-Host "=== Reset Complete ===" -ForegroundColor Green
Write-Host "Run 'start-dev.ps1' to start fresh" -ForegroundColor Cyan
