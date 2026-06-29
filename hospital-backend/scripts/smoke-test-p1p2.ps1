# P1/P2 smoke test
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

Test-Api 'statistics/analysis' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/statistics/analysis" -Headers $h
    if ($null -eq $r.data.visitTrend) { throw 'missing visitTrend' }
}

Test-Api 'statistics/reports' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/statistics/reports" -Headers $h
    if ($r.data.list.Count -lt 1) { throw 'empty reports' }
}

Test-Api 'statistics/decision' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/statistics/decision" -Headers $h
    if ($null -eq $r.data.suggestions) { throw 'missing suggestions' }
}

Test-Api 'department/tree' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/admin/department/tree" -Headers $h
    if ($r.data.Count -lt 1) { throw 'empty tree' }
}

Test-Api 'reimbursement/1/detail' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/reimbursement/1/detail" -Headers $h
    if ($null -eq $r.data.workflow) { throw 'missing workflow' }
}

Test-Api 'settlement/1/detail' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/settlement/1/detail" -Headers $h
    if ($null -eq $r.data.feeItems) { throw 'missing feeItems' }
}

Test-Api 'settlement/1/settle' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/settlement/1/settle" -Method POST -Headers $h -ContentType 'application/json' -Body '{}'
    if (-not $r.data.invoiceNo) { throw 'missing invoiceNo' }
}

Test-Api 'finance/3/flows after settle' {
    $r = Invoke-RestMethod -Uri "$($script:hisApi)/finance/3/flows" -Headers $h
    if ($r.data.list.Count -lt 1) { throw 'no settlement flow on account 3' }
}

Write-Host ''
if ($failed.Count -eq 0) {
    Write-Host 'P1/P2 smoke: ALL PASSED (7/7)' -ForegroundColor Green
    exit 0
} else {
    Write-Host "P1/P2 smoke: FAILED ($($failed.Count)/7)" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  - $_" }
    exit 1
}
