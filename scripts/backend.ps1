param([ValidateSet('run', 'test', 'package')][string]$Action = 'run')
$ErrorActionPreference = 'Stop'
$repoPath = Split-Path -Parent $PSScriptRoot
$configPath = Join-Path $repoPath '.env'
if (!(Test-Path -LiteralPath $configPath)) { throw 'Bitte .env gemäß README anlegen.' }
foreach ($line in [System.IO.File]::ReadAllLines($configPath)) {
    if ($line -match '^(POSTGRES_DB|POSTGRES_USER|POSTGRES_PASSWORD|POSTGRES_PORT)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
if (!$env:JAVA_HOME -and (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-21')) {
    $env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
}
Push-Location (Join-Path $repoPath 'backend')
try {
    $mavenGoal = switch ($Action) { 'run' { 'spring-boot:run' } 'test' { 'test' } 'package' { 'package' } }
    & .\mvnw.cmd --batch-mode --no-transfer-progress $mavenGoal
    if ($LASTEXITCODE -ne 0) { throw "Maven fehlgeschlagen (Exitcode $LASTEXITCODE)." }
} finally { Pop-Location }
