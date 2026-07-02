param(
    [switch]$KeepDocker
)

$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

Write-Host "=== Stopping Development Environment ===" -ForegroundColor Cyan

Write-Host "Stopping backend (Spring Boot)..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*spring-boot*"
} | Stop-Process -Force

Write-Host "Stopping frontend (Next.js)..." -ForegroundColor Yellow
Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*next*"
} | Stop-Process -Force

if (-not $KeepDocker) {
    Write-Host "Stopping Docker services..." -ForegroundColor Yellow
    docker compose -f "$RootDir\infrastructure\docker\dev\docker-compose.yml" down
}

Write-Host "=== Development Environment Stopped ===" -ForegroundColor Green
