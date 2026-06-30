# Start backend -> verify DeepSeek AI -> stop process (free port 8080 for IDEA)
param(
    [string]$ApiKey = '',
    [int]$StartupTimeoutSec = 90,
    [string]$BackendRoot = (Join-Path $PSScriptRoot '..\..\hospital-backend')
)

$ErrorActionPreference = 'Stop'
$BackendRoot = (Resolve-Path $BackendRoot).Path
$root = 'http://127.0.0.1:8080/api'

function Stop-BackendOnPort {
    param([int]$Port = 8080)
    $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $proc = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
        if ($proc -and $proc.ProcessName -match 'java') {
            Write-Host "Stopping Java on port $Port PID=$($proc.Id)"
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
        }
    }
}

function Wait-ForBackend {
    param([int]$TimeoutSec = 90)
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $r = Invoke-RestMethod -Uri ($root + '/common/departments') -TimeoutSec 3
            if ($r.code -eq 200) { return $true }
        } catch { }
        Start-Sleep -Seconds 2
    }
    return $false
}

if ($ApiKey) {
    & (Join-Path $PSScriptRoot 'setup-ai-secrets.ps1') -ApiKey $ApiKey -BackendRoot $BackendRoot
} elseif (-not (Test-Path (Join-Path $BackendRoot 'application-local.yml'))) {
    throw 'Missing application-local.yml. Pass -ApiKey or run setup-ai-secrets.ps1 first.'
}

Stop-BackendOnPort -Port 8080

if ($ApiKey) {
    $env:DEEPSEEK_API_KEY = $ApiKey
}

Write-Host 'Starting backend (prod + application-local.yml)...'
$logFile = Join-Path $BackendRoot 'target\verify-deepseek.log'
$errFile = Join-Path $BackendRoot 'target\verify-deepseek.err.log'
$proc = $null

Push-Location $BackendRoot
try {
    $proc = Start-Process -FilePath 'mvn' `
        -ArgumentList @('-q', 'spring-boot:run', '-Dspring-boot.run.profiles=prod') `
        -WorkingDirectory $BackendRoot `
        -PassThru -NoNewWindow `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $errFile

    if (-not (Wait-ForBackend -TimeoutSec $StartupTimeoutSec)) {
        throw "Backend not ready within ${StartupTimeoutSec}s. See $logFile"
    }
    Write-Host 'Backend is up.'

    $loginBody = @{ username = 'patient'; password = '123456' } | ConvertTo-Json -Compress
    $login = Invoke-RestMethod -Uri ($root + '/auth/login') -Method POST `
        -ContentType 'application/json' -Body $loginBody
    if ($login.code -ne 200) { throw "Login failed: $($login.message)" }

    $headers = @{
        Authorization = 'Bearer ' + $login.data.token
        'Content-Type' = 'application/json'
    }

    $status = Invoke-RestMethod -Uri ($root + '/ai/status') -Headers $headers
    if ($status.code -ne 200 -or $status.data.configured -ne $true) {
        throw 'AI key not loaded. Check application-local.yml and working directory.'
    }
    Write-Host '[OK] AI key configured (demoMode=false)' -ForegroundColor Green

    $records = Invoke-RestMethod -Uri ($root + '/record/list?pageSize=10') -Headers $headers
    if (-not $records.data.list -or $records.data.list.Count -lt 1) {
        throw 'No unlocked medical records. Complete visit + payment first.'
    }
    $recordId = $records.data.list[0].id
    Write-Host "Testing AI consult with recordId=$recordId ..."

    $question = 'Based on this visit record, give 3 brief post-visit care tips in Chinese.'
    $aiBody = @{
        recordId = $recordId
        messages = @(
            @{ role = 'user'; content = $question }
        )
    } | ConvertTo-Json -Depth 5 -Compress

    try {
        $ai = Invoke-RestMethod -Uri ($root + '/ai/consult') -Method POST -Headers $headers -Body $aiBody
        if ($ai.code -eq 200 -and $ai.data.reply) {
            $preview = $ai.data.reply
            if ($preview.Length -gt 120) { $preview = $preview.Substring(0, 120) + '...' }
            Write-Host '[OK] DeepSeek AI consult succeeded' -ForegroundColor Green
            Write-Host "Reply preview: $preview" -ForegroundColor Gray
        } elseif ($ai.code -eq 402) {
            Write-Host '[WARN] Key OK but DeepSeek balance insufficient (402). Recharge at platform.deepseek.com' -ForegroundColor Yellow
        } else {
            throw "AI consult failed: code=$($ai.code) msg=$($ai.message)"
        }
    } catch {
        $errMsg = $_.Exception.Message
        if ($errMsg -match '402|余额|Balance') {
            Write-Host '[WARN] Key OK but DeepSeek balance insufficient. Recharge to enable live replies.' -ForegroundColor Yellow
        } else {
            throw
        }
    }
}
finally {
    Pop-Location
    if ($proc -and -not $proc.HasExited) {
        Write-Host "Stopping backend PID=$($proc.Id)"
        Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    }
    Stop-BackendOnPort -Port 8080
    Write-Host 'Port 8080 released. You can start HospitalBackend (prod) in IDEA.' -ForegroundColor Green
}
