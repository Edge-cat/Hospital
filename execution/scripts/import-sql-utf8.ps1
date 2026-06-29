# UTF-8 safe MySQL import (avoid PowerShell pipe mojibake)
param(
    [Parameter(Mandatory = $true)]
    [string]$SqlFile,
    [string]$Container = "hospital-mysql",
    [string]$Database = "hospital_his"
)
$ErrorActionPreference = "Stop"
$resolved = (Resolve-Path $SqlFile).Path
$target = "/tmp/import.sql"
Write-Host "Copy $resolved -> ${Container}:$target" -ForegroundColor Cyan
docker cp $resolved "${Container}:${target}"
docker exec $Container mysql -uroot -proot --default-character-set=utf8mb4 $Database -e "source $target"
Write-Host "Done." -ForegroundColor Green
