# 三端前端生产构建
$ErrorActionPreference = "Stop"
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent

Write-Host "Building hospital-admin..." -ForegroundColor Cyan
Push-Location "$root\hospital-admin"
npm ci 2>$null; if ($LASTEXITCODE -ne 0) { npm install }
npm run build
Pop-Location

Write-Host "Building hospital-user..." -ForegroundColor Cyan
Push-Location "$root\hospital-user"
npm ci 2>$null; if ($LASTEXITCODE -ne 0) { npm install }
npm run build
Pop-Location

Write-Host "Building hospital-mini (H5)..." -ForegroundColor Cyan
Push-Location "$root\hospital-mini"
npm ci 2>$null; if ($LASTEXITCODE -ne 0) { npm install }
npm run build:h5 2>$null
if ($LASTEXITCODE -ne 0) { npx uni build 2>$null }
Pop-Location

Write-Host "Done. Outputs:" -ForegroundColor Green
Write-Host "  admin: $root\hospital-admin\dist"
Write-Host "  user:  $root\hospital-user\dist"
Write-Host "  mini:  $root\hospital-mini\dist"
