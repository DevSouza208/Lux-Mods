#Requires -Version 5.1
<#
  Builds the full Lux Mods development pack without GitHub Actions.
  Run from the repository root:
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-testpack-windows.ps1
  Requires JDK 21, internet for Gradle dependencies, and the Gradle wrapper.
#>
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$wrapper = Join-Path $root 'gradlew.bat'
if (-not (Test-Path $wrapper)) { throw 'Gradle wrapper missing from repository.' }
Push-Location $root
try {
    $javaVersion = (& java -version 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $javaVersion -notmatch 'version "21[."]') {
        throw 'JDK 21 is required. Check java -version and JAVA_HOME.'
    }
    & $wrapper --no-daemon assembleTestPack
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed (exit $LASTEXITCODE). See errors above." }

    $output = Join-Path $root 'build\lux-testpack\mods'
    $mods = @('lux-engine', 'lux-discovery', 'lux-atlas', 'lux-hud',
              'lux-menu', 'lux-borderless', 'lux-dialogues')
    $files = @(Get-ChildItem -LiteralPath $output -Filter '*.jar' -File)
    foreach ($mod in $mods) {
        $matching = @($files | Where-Object { $_.Name -match "^$([regex]::Escape($mod))-[0-9]" })
        if ($matching.Count -ne 1) {
            throw "Expected exactly one $mod JAR; found $($matching.Count)."
        }
        if ($matching[0].Length -lt 1024) {
            throw "JAR appears empty: $($matching[0].Name)"
        }
    }
    if ($files.Count -ne $mods.Count) {
        throw "Expected $($mods.Count) JARs; found $($files.Count)."
    }
    $manifest = Join-Path $root 'build\lux-testpack\SHA256SUMS.txt'
    $files | Sort-Object Name | ForEach-Object {
        $hash = Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256
        "$($hash.Hash.ToLowerInvariant())  $($_.Name)"
    } | Set-Content -Path $manifest -Encoding UTF8
    Write-Host "Lux Mods test pack built: $output" -ForegroundColor Green
    Write-Host "SHA256 manifest: $manifest"
} finally {
    Pop-Location
}
