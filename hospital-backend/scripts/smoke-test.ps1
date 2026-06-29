# Hospital API smoke test (requires backend on localhost:8080)
$root = 'http://localhost:8080/api'
$failed = @()

function Test-Api {
    param([string]$Name, [scriptblock]$Block)
    try {
        & $Block | Out-Null
        Write-Host "[OK] $Name" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] $Name - $_" -ForegroundColor Red
        $script:failed += $Name
    }
}

$adminLogin = Invoke-RestMethod -Uri ($root + '/auth/login') -Method POST -ContentType 'application/json' -Body '{"username":"admin","password":"123456"}'
$h = @{ Authorization = ('Bearer ' + $adminLogin.data.token) }

$patientLogin = Invoke-RestMethod -Uri ($root + '/auth/login') -Method POST -ContentType 'application/json' -Body '{"username":"patient","password":"123456"}'
$ph = @{ Authorization = ('Bearer ' + $patientLogin.data.token) }

Test-Api 'admin login' { if ($adminLogin.code -ne 200) { throw ('code ' + $adminLogin.code) } }
Test-Api 'patient login' { if ($patientLogin.code -ne 200) { throw ('code ' + $patientLogin.code) } }

Test-Api 'common/meta' {
    $r = Invoke-RestMethod -Uri ($root + '/common/meta')
    if (-not $r.data.options) { throw 'missing options' }
}

Test-Api 'statistics/overview' {
    $r = Invoke-RestMethod -Uri ($root + '/statistics/overview') -Headers $h
    if ($null -eq $r.data.patientCount) { throw 'missing patientCount' }
}

Test-Api 'admin/department/list' {
    $r = Invoke-RestMethod -Uri ($root + '/admin/department/list') -Headers $h
    if ($r.data.list.Count -lt 1) { throw 'empty list' }
}

Test-Api 'admin/menu/tree' {
    $r = Invoke-RestMethod -Uri ($root + '/admin/menu/tree') -Headers $h
    if ($r.data.Count -lt 1) { throw 'empty tree' }
}

Test-Api 'admin/config/list' {
    $r = Invoke-RestMethod -Uri ($root + '/admin/config/list') -Headers $h
    if (-not $r.data[0].configName) { throw 'missing configName' }
}

Test-Api 'patient/list' {
    $r = Invoke-RestMethod -Uri ($root + '/patient/list') -Headers $h
    if ($r.data.list.Count -lt 1) { throw 'empty' }
}

Test-Api 'patient/info' {
    $r = Invoke-RestMethod -Uri ($root + '/patient/info') -Headers $ph
    if (-not $r.data.name) { throw 'missing name' }
}

Test-Api 'doctor/list' {
    $r = Invoke-RestMethod -Uri ($root + '/doctor/list') -Headers $ph
    if ($r.data.list.Count -lt 1) { throw 'empty' }
}

Test-Api 'notice/list' {
    $r = Invoke-RestMethod -Uri ($root + '/notice/list') -Headers $ph
    if ($r.data.list.Count -lt 1) { throw 'empty' }
}

Test-Api 'register/list' {
    $r = Invoke-RestMethod -Uri ($root + '/register/list') -Headers $ph
    if ($null -eq $r.data.list) { throw 'missing list' }
}

Test-Api 'appointment/schedule' {
    $r = Invoke-RestMethod -Uri ($root + '/appointment/schedule?doctorId=1') -Headers $ph
    if ($r.data.dates.Count -lt 1) { throw 'missing dates' }
}

Test-Api 'payment/list' {
    $r = Invoke-RestMethod -Uri ($root + '/payment/list') -Headers $ph
    if ($null -eq $r.data.list) { throw 'missing list' }
}

Test-Api 'payment/summary' {
    $r = Invoke-RestMethod -Uri ($root + '/payment/summary') -Headers $ph
    if ($null -eq $r.data.count) { throw 'missing count' }
}

Test-Api 'record/list' {
    $r = Invoke-RestMethod -Uri ($root + '/record/list') -Headers $ph
    if ($r.data.list.Count -lt 1) { throw 'empty' }
}

Test-Api 'income-expense/summary' {
    $r = Invoke-RestMethod -Uri ($root + '/income-expense/summary') -Headers $h
    if ($null -eq $r.data.income) { throw 'missing income' }
}

Test-Api 'reimbursement/1/detail' {
    $r = Invoke-RestMethod -Uri ($root + '/reimbursement/1/detail') -Headers $h
    if (-not $r.data.workflow) { throw 'missing workflow' }
}

Write-Host ''
$pass = 18 - $failed.Count
if ($failed.Count -eq 0) {
    Write-Host "Basic smoke: $pass/18 passed" -ForegroundColor Cyan
    exit 0
} else {
    Write-Host ("Failed: " + ($failed -join ', ')) -ForegroundColor Red
    exit 1
}
