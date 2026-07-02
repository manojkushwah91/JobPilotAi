param(
    [string]$Environment = "dev",
    [string]$Profile = "dev"
)

$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

Write-Host "=== Database Migration & Seed ===" -ForegroundColor Cyan
Write-Host "Environment: $Environment" -ForegroundColor Yellow

# Run Flyway migration
Write-Host "Running Flyway migration..." -ForegroundColor Yellow
& "$RootDir\backend\mvnw.cmd" flyway:migrate `
    -f "$RootDir\backend\pom.xml" `
    -Dflyway.url="jdbc:postgresql://localhost:5432/jobpilot" `
    -Dflyway.user="jobpilot" `
    -Dflyway.password="jobpilot_dev" `
    -Dflyway.locations="classpath:db/migration/$Environment" `
    -Dflyway.baselineOnMigrate="true"

Write-Host "=== Database seeding complete ===" -ForegroundColor Green
