
# Execute na raiz: powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-environment.ps1
# Nao instala Minecraft, nao mexe em saves.
$ErrorActionPreference = "Stop"
Set-Location (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Write-Host "=== LUX MODS / JAVA 21 ===" -ForegroundColor Cyan
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw "Java nao encontrado no PATH" }
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw "JDK ausente: javac nao encontrado no PATH" }
& java -version
& javac -version
if (!(Test-Path "gradlew.bat") -or !(Test-Path "gradle/wrapper/gradle-wrapper.jar")) {
    throw "Wrapper Gradle nao encontrado. Confirme o checkout da branch feat/engine-discovery-bootstrap."
}
Write-Host "=== SMOKE JAVA (SEM INTERNET) ===" -ForegroundColor Cyan
& cmd.exe /c "scripts\smoke-pure-java.bat"
if ($LASTEXITCODE -ne 0) { throw "Smoke Java falhou: codigo $LASTEXITCODE" }
Write-Host "=== BUILD NEOFORGE (PODE BAIXAR DEPENDENCIAS) ===" -ForegroundColor Cyan
& cmd.exe /c "gradlew.bat :lux-engine:build :lux-discovery:build --no-daemon --stacktrace"
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Build falhou. Envie logs sanitizados para PR #14."
    exit $LASTEXITCODE
}
Write-Host "=== JARS GERADOS ===" -ForegroundColor Green
Get-ChildItem -Path @("modules/lux-engine/build/libs","modules/lux-discovery/build/libs") -Filter "*.jar" -ErrorAction Stop | Select-Object FullName, Length
Write-Host "Ainda falta testar os JARs no Minecraft e no servidor dedicado."
