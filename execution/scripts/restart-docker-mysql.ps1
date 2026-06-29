# Rebuild MySQL Docker with UTF-8 full seed (fix Chinese mojibake)
$ErrorActionPreference = "Stop"
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$exec = Join-Path $root "execution"

Write-Host "Starting Docker Desktop..." -ForegroundColor Cyan
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe" -ErrorAction SilentlyContinue
$deadline = (Get-Date).AddSeconds(90)
while ((Get-Date) -lt $deadline) {
    docker info 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { Write-Host "Docker ready"; break }
    Start-Sleep -Seconds 5
}
if ($LASTEXITCODE -ne 0) { throw "Docker Desktop not available" }

Write-Host "Removing old MySQL container/volume..." -ForegroundColor Cyan
docker rm -f hospital-mysql 2>$null | Out-Null
docker volume rm execution_hospital_mysql_data hospital_mysql_data 2>$null | Out-Null

Push-Location $exec
docker compose up -d mysql
Pop-Location

Write-Host "Waiting for MySQL healthy..." -ForegroundColor Cyan
$ok = $false
for ($i = 0; $i -lt 60; $i++) {
    $h = docker inspect --format='{{.State.Health.Status}}' hospital-mysql 2>$null
    if ($h -eq 'healthy') { $ok = $true; break }
    Start-Sleep -Seconds 3
}
if (-not $ok) { throw "MySQL did not become healthy in time" }

$verifySql = "USE hospital_his; SELECT HEX(name) FROM sys_user WHERE username='admin';"
$hex = docker exec hospital-mysql mysql -uroot -proot --default-character-set=utf8mb4 -N -e $verifySql 2>$null
Write-Host "admin.name HEX = $hex" -ForegroundColor Green
# E7B3BBE7BB9FE7AEA1E79086E59198 = 系统管理员
if ($hex -ne 'E7B3BBE7BB9FE7AEA1E79086E59198') {
    Write-Host "Re-importing SQL via docker cp..." -ForegroundColor Yellow
    $sql = Join-Path $exec "database\hospital_his_full.sql"
    docker cp $sql hospital-mysql:/tmp/full.sql
    docker exec hospital-mysql mysql -uroot -proot --default-character-set=utf8mb4 -e "DROP DATABASE IF EXISTS hospital_his;" 2>$null
    docker exec hospital-mysql mysql -uroot -proot --default-character-set=utf8mb4 hospital_his -e "source /tmp/full.sql" 2>$null
    $hex = docker exec hospital-mysql mysql -uroot -proot --default-character-set=utf8mb4 -N -e $verifySql 2>$null
    Write-Host "admin.name HEX after re-import = $hex" -ForegroundColor Green
}

Write-Host "MySQL ready on 127.0.0.1:3307" -ForegroundColor Green
