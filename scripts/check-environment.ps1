
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
if (Get-Command python -ErrorAction SilentlyContinue) {
    Write-Host "=== PREFLIGHT (SEM GRADLE OU MINECRAFT) ===" -ForegroundColor Cyan
    & python scripts/preflight.py
    if ($LASTEXITCODE -ne 0) { throw "Preflight falhou. Corrija o workspace antes do build." }
    & python -m unittest discover -s scripts -p "test_preflight.py" -v
    if ($LASTEXITCODE -ne 0) { throw "Teste do verificador offline falhou." }
} else {
    Write-Warning "Python indisponivel: pulando auditoria estrutural. JDK/Gradle ainda serao testados."
}
Write-Host "=== SMOKE JAVA (SEM INTERNET) ===" -ForegroundColor Cyan
& cmd.exe /c "scripts\smoke-pure-java.bat"
if ($LASTEXITCODE -ne 0) { throw "Smoke Java falhou: codigo $LASTEXITCODE" }
Write-Host "=== BUILD NEOFORGE (PODE BAIXAR DEPENDENCIAS) ===" -ForegroundColor Cyan
& cmd.exe /c "gradlew.bat assembleTestPack --no-daemon --stacktrace"
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Build falhou. Envie logs sanitizados para PR #14."
    exit $LASTEXITCODE
}
Write-Host "=== JARS GERADOS PARA TESTE ===" -ForegroundColor Green
$Playtest = "build/lux-testpack/mods"
if (!(Test-Path $Playtest)) { throw "Gradle terminou mas a pasta do pacote nao foi criada: $Playtest" }
Get-ChildItem -Path $Playtest -Filter "*.jar" | Select-Object Name, Length
if (Get-Command python -ErrorAction SilentlyContinue) {
    Write-Host "=== VALIDACAO DE JARS (PYTHON 3.11+) ===" -ForegroundColor Cyan
    & python scripts/verify_jars.py
    if ($LASTEXITCODE -ne 0) { throw "Metadados dos JARs inconsistentes" }
} else {
    Write-Warning "Python nao encontrado; execute o verificador depois se desejar, ou confira os JARs manualmente."
}
Get-ChildItem -Path @("modules/lux-engine/build/libs","modules/lux-discovery/build/libs") -Filter "*.jar" -ErrorAction Stop | Select-Object FullName, Length
Write-Host "Pacote pronto em $Playtest. Ainda falta testar no Minecraft e servidor dedicado." -ForegroundColor Yellow
