# Execute pelo PowerShell:
# powershell -NoProfile -ExecutionPolicy Bypass -File scripts\install-to-sklauncher.ps1
# Instala APENAS na instancia 'teste' do SKlauncher, se o Gradle concluir sem erros.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$mods = Join-Path $env:APPDATA '.sklauncher\instances\teste\mods'
$pack = Join-Path $root 'build\lux-testpack\mods'
$gradle = Join-Path $root 'gradlew.bat'

if (-not (Test-Path $mods -PathType Container)) {
    throw "Pasta mods nao encontrada: $mods. Abra a instancia teste primeiro."
}
if (-not (Test-Path $gradle -PathType Leaf)) {
    throw "Wrapper Gradle nao encontrado: $gradle"
}
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'JDK 21 nao encontrado no PATH. Abra um novo PowerShell apos a instalacao.'
}
$version = (& javac -version 2>&1 | Out-String).Trim()
if ($version -notmatch '^javac 21(\.|$)') {
    throw "Precisa do compilador JDK 21; encontrado: $version"
}
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    throw 'JAVA_HOME invalido. Configure a pasta de instalacao do JDK 21.'
}
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
# No PowerShell 5.1, "java -version" escreve no stderr, e com
# ErrorActionPreference=Stop pode virar NativeCommandError mesmo com Java ok.
# "java --version" (JDK 9+) escreve no stdout.
$running = (& java --version | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $running -notmatch '(?m)^(openjdk|java) 21(\.|\s|$)') {
    throw "O Java que executa o Gradle nao e 21: $running"
}

Write-Host '=== LUX MODS: COMPILAR E INSTALAR NA INSTANCIA TESTE ===' -ForegroundColor Cyan
Write-Host "Java: $version | JAVA_HOME: $env:JAVA_HOME"
Push-Location $root
try {
    & $gradle assembleTestPack --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw 'Compilacao falhou. Nenhum JAR foi copiado. Envie o erro do terminal.'
    }
}
finally {
    Pop-Location
}

$files = @()
foreach ($pattern in @('lux-engine-*.jar', 'lux-discovery-*.jar', 'lux-atlas-*.jar', 'lux-hud-*.jar')) {
    $found = @(Get-ChildItem $pack -Filter $pattern -File -ErrorAction Stop |
        Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' })
    if ($found.Count -ne 1 -or $found[0].Length -le 0) {
        throw "Esperado exatamente 1 JAR valido para $pattern, encontrados: $($found.Count)"
    }
    $files += $found[0]
}

# Validacao basica dos metadados antes de tocar na pasta mods.
Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($file in $files) {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($file.FullName)
    try {
        $meta = $zip.GetEntry('META-INF/neoforge.mods.toml')
        $hasClass = @($zip.Entries | Where-Object { $_.FullName -match '^dev/luxmods/.+\.class$' }).Count -gt 0
        if ($null -eq $meta -or -not $hasClass) {
            throw "JAR invalido ou sem metadados/classes: $($file.Name)"
        }
    }
    finally {
        $zip.Dispose()
    }
}

# Copiar backups para FORA da pasta mods; outros mods nao sao alterados.
$old = @(Get-ChildItem $mods -Filter 'lux-engine-*.jar' -File) +
       @(Get-ChildItem $mods -Filter 'lux-discovery-*.jar' -File) +
       @(Get-ChildItem $mods -Filter 'lux-atlas-*.jar' -File) +
       @(Get-ChildItem $mods -Filter 'lux-hud-*.jar' -File)
$backup = Join-Path (Split-Path $mods -Parent) ('lux-mods-backup\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
if ($old.Count -gt 0) {
    New-Item -ItemType Directory -Force -Path $backup | Out-Null
    foreach ($file in $old) {
        Copy-Item $file.FullName -Destination $backup -Force
    }
}
try {
    foreach ($file in $old) { Remove-Item $file.FullName -Force }
    foreach ($file in $files) {
        Copy-Item $file.FullName -Destination $mods -Force
        Write-Host "INSTALADO: $($file.Name)" -ForegroundColor Green
    }
}
catch {
    foreach ($file in $files) {
        Remove-Item (Join-Path $mods $file.Name) -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Copy-Item (Join-Path $backup '*.jar') -Destination $mods -Force
    }
    throw
}
Write-Host "Pronto! Lux Engine + Discovery + Atlas + HUD instalados somente em $mods" -ForegroundColor Green
explorer.exe $mods
