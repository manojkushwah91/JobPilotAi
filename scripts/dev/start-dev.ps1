param(
    [switch]$NoDocker,
    [switch]$NoBackend,
    [switch]$NoFrontend
)

$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

Write-Host "=== JobPilot AI — Development Environment ===" -ForegroundColor Cyan

if (-not $NoDocker) {
    Write-Host "Starting Docker services..." -ForegroundColor Yellow
    docker compose -f "$RootDir\infrastructure\docker\dev\docker-compose.yml" up -d

    Write-Host "Waiting for PostgreSQL..." -ForegroundColor Yellow
    do {
        $result = docker compose -f "$RootDir\infrastructure\docker\dev\docker-compose.yml" exec -T postgres pg_isready -U jobpilot 2>$null
        Start-Sleep -Seconds 2
    } while ($LASTEXITCODE -ne 0)
    Write-Host "PostgreSQL is ready" -ForegroundColor Green
}

if (-not $NoBackend) {
    Write-Host "Starting backend (Spring Boot)..." -ForegroundColor Yellow
    $backendJob = Start-Job -ScriptBlock {
        param($dir)
        Set-Location $dir
        mvn spring-boot:run -Dspring-boot.run.profiles=dev
    } -ArgumentList "$RootDir\backend"
    Write-Host "Backend PID: $($backendJob.Id)" -ForegroundColor Green
}

if (-not $NoFrontend) {
    Write-Host "Starting frontend (Next.js)..." -ForegroundColor Yellow
    $frontendJob = Start-Job -ScriptBlock {
        param($dir)
        Set-Location $dir
        npm run dev
    } -ArgumentList "$RootDir\frontend"
    Write-Host "Frontend PID: $($frontendJob.Id)" -ForegroundColor Green
}

Write-Host "`n=== Development Environment Started ===" -ForegroundColor Cyan
Write-Host "Backend API:  http://localhost:8080" -ForegroundColor Green
Write-Host "Swagger UI:   http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "Frontend:     http://localhost:3000" -ForegroundColor Green
Write-Host "pgAdmin:      http://localhost:5050 (admin@jobpilot.dev / admin)" -ForegroundColor Green
Write-Host "MailHog:      http://localhost:8025" -ForegroundColor Green
Write-Host "Prometheus:   http://localhost:9090" -ForegroundColor Green
Write-Host "Grafana:      http://localhost:3000 (admin / admin)" -ForegroundColor Green
Write-Host "MinIO Console: http://localhost:9001 (jobpilot / jobpilot_dev)" -ForegroundColor Green
Write-Host "`nRun 'docker compose logs -f' to view logs" -ForegroundColor Gray
