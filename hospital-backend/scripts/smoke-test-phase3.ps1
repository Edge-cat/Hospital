# Phase 3 smoke test
$script:hisApi = 'http://localhost:8080/api'
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

$login = Invoke-RestMethod -Uri "$($script:hisApi)/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"admin","password":"123456"}'
$h = @{ Authorization = "Bearer $($login.data.token)" }

Test-Api 'menu/tree' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/admin/menu/tree" -Headers $h
    if ($r.data.Count -lt 7) { throw 'menu tree too small' }
}

Test-Api 'dict/list' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/admin/dict/list?page=1&pageSize=10&dictType=gender" -Headers $h
    if ($r.data.list.Count -lt 2) { throw 'gender dict missing' }
}

Test-Api 'finance/1/flows' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/finance/1/flows" -Headers $h
    if ($null -eq $r.data.list) { throw 'missing flows list' }
    if ($r.data.list.Count -lt 1) { throw 'empty flows for account 1' }
}

Test-Api 'finance/2/flows' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/finance/2/flows" -Headers $h
    if ($r.data.list.Count -lt 1) { throw 'empty flows for account 2' }
}

Test-Api 'common/meta cache' {
    $r1 = Invoke-RestMethod -Uri "$($script:hisApi)/common/meta"
    $r2 = Invoke-RestMethod -Uri "$($script:hisApi)/common/meta"
    if ($null -eq $r1.data) { throw 'meta empty' }
}

Test-Api 'auth/wx-login' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/auth/wx-login" -Method POST -ContentType 'application/json' -Body '{"code":"demo-code"}'
    if (-not $r.data.token) { throw 'no token' }
    if ($r.data.user.role -ne 'patient') { throw 'expected patient role' }
}

Write-Host ''
if ($failed.Count -eq 0) {
    Write-Host 'Phase3 smoke: ALL PASSED (6/6)' -ForegroundColor Green
    exit 0
} else {
    Write-Host "Phase3 smoke: FAILED ($($failed.Count)/6)" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  - $_" }
    exit 1
}
