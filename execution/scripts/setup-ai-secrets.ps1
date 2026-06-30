# Write DeepSeek secrets to gitignored application-local.yml
param(
    [Parameter(Mandatory = $true)]
    [string]$ApiKey,
    [string]$BackendRoot = (Join-Path $PSScriptRoot '..\..\hospital-backend')
)

$BackendRoot = (Resolve-Path $BackendRoot).Path
$outFile = Join-Path $BackendRoot 'application-local.yml'

$lines = @(
    '# Local secrets - gitignored, do not commit'
    'hospital:'
    '  ai:'
    '    enabled: true'
    "    api-key: $ApiKey"
    '    base-url: https://api.deepseek.com'
    '    model: deepseek-chat'
    '    max-tokens: 1200'
)

Set-Content -Path $outFile -Value ($lines -join "`n") -Encoding UTF8
Write-Host "Written: $outFile" -ForegroundColor Green
