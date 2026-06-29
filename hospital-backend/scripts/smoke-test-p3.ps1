# P3 security/audit smoke test
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

Test-Api 'login menuIds' {
    if ($null -eq $login.data.user.menuIds -or $login.data.user.menuIds.Count -lt 10) {
        throw 'admin menuIds missing'
    }
}

Test-Api 'bcrypt re-login' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"admin","password":"123456"}'
    if (-not $r.data.token) { throw 'bcrypt login failed' }
}

Test-Api 'reimbursement approve audit' {
    Invoke-RestMethod -Uri "$($script:hisApi)/reimbursement/1/approve" -Method POST -Headers $h -ContentType 'application/json' -Body '{}' | Out-Null
}

Test-Api 'operation log list' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/admin/log/operation?page=1&pageSize=10" -Headers $h
    if ($r.data.list.Count -lt 1) { throw 'operation log empty' }
}

Test-Api 'income-expense summary mom' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/income-expense/summary" -Headers $h
    if ($null -eq $r.data.incomeMom) { throw 'missing incomeMom' }
    if ($r.data.incomeTrend.Count -ne 7) { throw 'bad incomeTrend' }
}

Test-Api 'auth/wx-login' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/auth/wx-login" -Method POST -ContentType 'application/json' -Body '{"code":"demo-code"}'
    if (-not $r.data.token) { throw 'wx login failed' }
}

Write-Host ''
if ($failed.Count -eq 0) {
    Write-Host 'P3 smoke: ALL PASSED (6/6)' -ForegroundColor Green
    exit 0
} else {
    Write-Host "P3 smoke: FAILED ($($failed.Count)/6)" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  - $_" }
    exit 1
}
