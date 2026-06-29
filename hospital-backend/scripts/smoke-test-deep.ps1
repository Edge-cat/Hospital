# 深度操作 API 冒烟测试
$apiBase = "http://localhost:8080/api"
$failed = @()

function Test-Api($Name, $ScriptBlock) {
    try {
        & $ScriptBlock | Out-Null
        Write-Host "[OK] $Name" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] $Name - $_" -ForegroundColor Red
        $script:failed += $Name
    }
}

$login = Invoke-RestMethod -Uri "$apiBase/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"123456"}'
$h = @{ Authorization = "Bearer $($login.data.token)" }

Test-Api "schedule/calendar" {
    $r = Invoke-RestMethod -Uri "$apiBase/schedule/calendar" -Headers $h
    if ($r.data.list.Count -lt 1) { throw "empty calendar" }
}

Test-Api "schedule/1/affected" {
    $r = Invoke-RestMethod -Uri "$apiBase/schedule/1/affected" -Headers $h
    if ($null -eq $r.data.count) { throw "missing count" }
}

Test-Api "drug/1/inventory" {
    $r = Invoke-RestMethod -Uri "$apiBase/drug/1/inventory" -Headers $h
    if ($null -eq $r.data.totalStock) { throw "missing totalStock" }
}

Test-Api "drug/1/procurement-trend" {
    $r = Invoke-RestMethod -Uri "$apiBase/drug/1/procurement-trend" -Headers $h
    if ($null -eq $r.data.list) { throw "missing list" }
}

Test-Api "procurement/1/logistics" {
    $r = Invoke-RestMethod -Uri "$apiBase/procurement/1/logistics" -Headers $h
    if ($null -eq $r.data.timeline) { throw "missing timeline" }
}

Test-Api "finance/1/flows" {
    $r = Invoke-RestMethod -Uri "$apiBase/finance/1/flows" -Headers $h
    if ($null -eq $r.data.list) { throw "missing flows" }
}

Test-Api "dispensing/scan" {
    $r = Invoke-RestMethod -Uri "$apiBase/dispensing/scan?barcode=BC202606001" -Headers $h
    if (-not $r.data.barcode) { throw "missing barcode" }
}

Test-Api "record/1/chain" {
    $r = Invoke-RestMethod -Uri "$apiBase/record/1/chain" -Headers $h
    if ($r.data.steps.Count -lt 1) { throw "missing steps" }
}

Test-Api "service/1/toggle" {
    $body = '{"status":0}'
    $r = Invoke-RestMethod -Uri "$apiBase/service/1/toggle" -Method PUT -Headers $h -ContentType "application/json" -Body $body
    if ($r.code -ne 200) { throw "toggle failed" }
}

Test-Api "doctor/batch-import" {
    $body = '{"list":[{"name":"测试医生","department":"内科","title":"住院医师"}]}'
    $r = Invoke-RestMethod -Uri "$apiBase/doctor/batch-import" -Method POST -Headers $h -ContentType "application/json" -Body $body
    if ($r.code -ne 200) { throw "import failed" }
}

Write-Host ""
$total = 10
$pass = $total - $failed.Count
if ($failed.Count -eq 0) {
    Write-Host "Deep smoke: $pass/$total passed" -ForegroundColor Cyan
    exit 0
} else {
    Write-Host "Failed: $($failed -join ', ')" -ForegroundColor Red
    exit 1
}
