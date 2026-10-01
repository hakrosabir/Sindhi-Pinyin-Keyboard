param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$SdkRoot = $env:ANDROID_HOME,
    [string]$GradleHome,
    [switch]$Offline
)
# Build the actual Gradle project so application/version settings have one source of truth.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$savedJava = $env:JAVA_HOME
$savedAndroid = $env:ANDROID_HOME
Push-Location -LiteralPath $projectRoot
try {
    if ($JavaHome) { $env:JAVA_HOME = $JavaHome }
    if ($SdkRoot) { $env:ANDROID_HOME = $SdkRoot }
    $gradle = if ($GradleHome) { Join-Path $GradleHome 'bin/gradle.bat' } else { Join-Path $projectRoot 'gradlew.bat' }
    if (!(Test-Path -LiteralPath $gradle)) { throw "Gradle launcher missing: $gradle" }
    $buildArgs = @('--no-daemon', ':engine:test', ':app:testDebugUnitTest', ':app:lintDebug', ':app:assembleDebug')
    if ($Offline) { $buildArgs += '--offline' }
    & $gradle @buildArgs
    if ($LASTEXITCODE -ne 0) { throw 'Gradle verification failed; no APK was packaged.' }

    $output = Join-Path $projectRoot 'app/build/outputs/apk/debug'
    $metadata = Get-Content -LiteralPath (Join-Path $output 'output-metadata.json') -Raw | ConvertFrom-Json
    if (@($metadata.elements).Count -ne 1) { throw 'Expected one universal debug APK.' }
    $artifact = $metadata.elements[0]
    $version = $artifact.versionName
    if ($version -notmatch '^[A-Za-z0-9._-]+$') { throw 'Unexpected APK version name.' }
    $sourceApk = Join-Path $output $artifact.outputFile
    if (!(Test-Path -LiteralPath $sourceApk)) { throw 'Built APK is missing.' }
    $dist = Join-Path $projectRoot 'dist'
    New-Item -ItemType Directory -Force -Path $dist | Out-Null
    $apkPath = Join-Path $dist "Sindhi-Pinyin-Keyboard-$version.apk"
    Copy-Item -LiteralPath $sourceApk -Destination $apkPath -Force
    $hash = (Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash.ToLowerInvariant()
    [IO.File]::WriteAllText("$apkPath.sha256", "$hash  $([IO.Path]::GetFileName($apkPath))`n")
    Write-Output "Development APK: $apkPath"
    Write-Output "SHA-256: $hash"
} finally {
    $env:JAVA_HOME = $savedJava
    $env:ANDROID_HOME = $savedAndroid
    Pop-Location
}
