# Atualizador local Lux Mods para SKlauncher (PowerShell 5.1, JDK 21).
param([string]$Instance = 'lux-mods')
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$mods = Join-Path $env:APPDATA ('.sklauncher\instances\' + $Instance + '\mods')
$pack = Join-Path $root 'build\lux-testpack\mods'
$gradle = Join-Path $root 'gradlew.bat'
$names = @('lux-engine','lux-discovery','lux-compass','lux-quests','lux-upgrades','lux-hud','lux-menu','lux-borderless','lux-dialogues')
$obsolete = @('lux-atlas')
$allNames = @($names) + @($obsolete)
Write-Host "INSTANCIA: $Instance / $mods" -ForegroundColor Cyan
if (-not (Test-Path $mods -PathType Container)) { throw "Instancia nao encontrada: $mods" }
if (-not (Test-Path $gradle -PathType Leaf)) { throw "Gradle wrapper ausente: $gradle" }
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Git nao encontrado no PATH.' }
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe') -PathType Leaf) -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe') -PathType Leaf)) {
    throw 'JAVA_HOME deve apontar para um JDK 21 completo.'
}
Push-Location $root
try {
    $dirty = (& git status --porcelain | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao consultar status Git.' }
    if ($dirty) { throw 'Checkout possui mudancas locais; atualizacao interrompida para preservar arquivos.' }
    $branch = (& git branch --show-current | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao consultar branch.' }
    if ($branch -ne 'main') {
        & git switch main
        if ($LASTEXITCODE -ne 0) { throw 'Falha ao trocar para main.' }
    }
    & git pull --ff-only origin main
    if ($LASTEXITCODE -ne 0) { throw 'git pull falhou. Nenhum JAR foi instalado.' }
    $commit = (& git rev-parse --short HEAD | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao consultar commit.' }
} finally { Pop-Location }
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
$version = (& (Join-Path $env:JAVA_HOME 'bin\javac.exe') -version 2>&1 | Out-String).Trim()
if ($version -notmatch '^javac 21(\.|$)') { throw "Exigido JDK 21: $version" }
$running = (& (Join-Path $env:JAVA_HOME 'bin\java.exe') -version 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0 -or $running -notmatch 'version "21(\.|")') { throw "Java 21 nao confirmado: $running" }
Write-Host "Compilando localmente main @$commit..." -ForegroundColor Yellow
Push-Location $root
try {
    & $gradle assembleTestPack --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'BUILD FALHOU; instalacao preservada.' }
} finally { Pop-Location }
if (-not (Test-Path $pack -PathType Container)) { throw "Pacote nao encontrado: $pack" }
$files = @()
foreach ($name in $names) {
    $found = @(Get-ChildItem $pack -Filter "$name-*.jar" -File | Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' })
    if ($found.Count -ne 1 -or $found[0].Length -le 0) { throw "Esperado um JAR valido para $name; encontrados $($found.Count)." }
    $files += $found[0]
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($file in $files) {
    $zip = [System.IO.Compression.ZipFile]::OpenRead($file.FullName)
    try {
        $meta = $zip.GetEntry('META-INF/neoforge.mods.toml')
        $classes = @($zip.Entries | Where-Object { $_.FullName -match '^dev/luxmods/.+\.class$' }).Count
        if ($null -eq $meta -or $classes -eq 0) { throw "JAR sem metadata/classes: $($file.Name)" }
    } finally { $zip.Dispose() }
}
function Is-LuxJar([string]$fileName) {
    foreach ($name in $allNames) {
        if ($fileName -match ('^' + [regex]::Escape($name) + '(-.*)?\.jar$')) { return $true }
    }
    return $false
}
$old = @(Get-ChildItem $mods -Filter '*.jar' -File | Where-Object { Is-LuxJar $_.Name })
$backup = Join-Path (Split-Path $mods -Parent) ('lux-mods-backup\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
if ($old.Count -gt 0) {
    New-Item -ItemType Directory -Force -Path $backup | Out-Null
    foreach ($file in $old) { Copy-Item -LiteralPath $file.FullName -Destination $backup -Force }
}
try {
    foreach ($file in $old) { Remove-Item -LiteralPath $file.FullName -Force }
    foreach ($file in $files) {
        Copy-Item -LiteralPath $file.FullName -Destination $mods -Force
        $dest = Join-Path $mods $file.Name
        $actual = (Get-FileHash -LiteralPath $dest -Algorithm SHA256).Hash
        $expected = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash
        if ($actual -ne $expected) { throw "SHA256 incorreto: $($file.Name)" }
        Write-Host "OK: $($file.Name) sha256=$($actual.Substring(0,12))" -ForegroundColor Green
    }
    $installed = @(Get-ChildItem $mods -Filter '*.jar' -File | Where-Object { Is-LuxJar $_.Name })
    if ($installed.Count -ne $names.Count) { throw "Quantidade incorreta de JARs Lux: $($installed.Count)" }
    foreach ($name in $names) {
        if (@($installed | Where-Object { $_.Name -match ('^' + [regex]::Escape($name) + '(-.*)?\.jar$') }).Count -ne 1) {
            throw "JAR ausente ou duplicado: $name"
        }
    }
    if (@($installed | Where-Object { $_.Name -match '^lux-atlas(-.*)?\.jar$' }).Count -ne 0) { throw 'Atlas antigo presente.' }
} catch {
    Get-ChildItem $mods -Filter '*.jar' -File -ErrorAction SilentlyContinue |
        Where-Object { Is-LuxJar $_.Name } | Remove-Item -Force -ErrorAction SilentlyContinue
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $mods -Force }
    }
    throw
}
Write-Host "INSTALACAO CONFERIDA: main @$commit / $Instance" -ForegroundColor Green
explorer.exe $mods
