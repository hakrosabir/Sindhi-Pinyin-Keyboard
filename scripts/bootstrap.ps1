param([string]$Version = '9.6.0')
$ErrorActionPreference = 'Stop'
if ($Version -ne '9.6.0') { throw 'Update and review the project build versions together before changing Gradle.' }
$projectRoot = Split-Path -Parent $PSScriptRoot
$toolsPath = Join-Path $projectRoot '.tools'
New-Item -ItemType Directory -Force -Path $toolsPath | Out-Null
$zipPath = Join-Path $toolsPath "gradle-$Version-bin.zip"
$shaPath = "$zipPath.sha256"
$baseUrl = "https://services.gradle.org/distributions/gradle-$Version-bin.zip"
Invoke-WebRequest -UseBasicParsing -Uri "$baseUrl.sha256" -OutFile $shaPath
Invoke-WebRequest -UseBasicParsing -Uri $baseUrl -OutFile $zipPath
$expected = (Get-Content -LiteralPath $shaPath -Raw).Trim().ToLowerInvariant()
$actual = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash.ToLowerInvariant()
if ($expected -notmatch '^[0-9a-f]{64}$' -or $expected -ne $actual) { throw 'Gradle checksum mismatch.' }
Expand-Archive -LiteralPath $zipPath -DestinationPath $toolsPath -Force
$gradlePath = Join-Path $toolsPath "gradle-$Version\bin\gradle.bat"
Push-Location $projectRoot
try {
    & $gradlePath wrapper --gradle-version $Version --distribution-type bin --gradle-distribution-sha256-sum $expected
    if ($LASTEXITCODE -ne 0) { throw 'Gradle wrapper generation failed; check Java and dependency connectivity.' }
} finally { Pop-Location }
