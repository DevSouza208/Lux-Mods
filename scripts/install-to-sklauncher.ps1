# Atualizador Lux Mods para SKlauncher, Windows PowerShell 5.1 / Java 21.
# ATENCAO: compila LOCALMENTE; nao usa CI nem GitHub Actions.
param([string]$Instance = 'lux-mods')
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$mods = Join-Path $env:APPDATA ('.sklauncher\instances\' + $Instance + '\mods')
$pack = Join-Path $root 'build\lux-testpack\mods'
$gradle = Join-Path $root 'gradlew.bat'
$names = @('lux-engine', 'lux-discovery', 'lux-compass', 'lux-quests', 'lux-upgrades',
           'lux-hud', 'lux-menu', 'lux-borderless', 'lux-dialogues')
# These JARs are OBSOLETE and must never be left in the game.
$obsolete = @('lux-atlas')
Write-Host "INSTANCIA DESTINO: $Instance" -ForegroundColor Cyan
Write-Host "PASTA MODS: $mods" -ForegroundColor Cyan
if (-not (Test-Path $mods -PathType Container)) {
    throw "Instancia nao encontrada: $mods. Nada foi instalado. Execute com -Instance <nome> se diferente."
}
if (-not (Test-Path $gradle -PathType Leaf)) { throw "Gradle wrapper nao encontrado: $gradle" }
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Git nao encontrado no PATH.' }
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe') -PathType Leaf) -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe') -PathType Leaf)) {
    throw 'JAVA_HOME deve apontar para um JDK 21 completo (java.exe e javac.exe).'
}

# Nunca afirmar que atualizou quando apenas recompilou um checkout antigo.
Push-Location $root
try {
    $branch = (& git branch --show-current | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel identificar a branch Git.' }
    if ($branch -ne 'main') {
        $dirty = (& git status --porcelain | Out-String).Trim()
        if ($dirty) { throw "Checkout em $branch com mudancas locais. Nao vou trocar de branch." }
        & git switch main
        if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel mudar para main.' }
    }
    Write-Host 'Buscando codigo atual da main...' -ForegroundColor Yellow
    & git pull --ff-only origin main
    if ($LASTEXITCODE -ne 0) { throw 'git pull falhou; nao sera instalada uma versao desatualizada.' }
    $commit = (& git rev-parse --short HEAD | Out-String).Trim()
    Write-Host "CODIGO FONTE: main @$commit" -ForegroundColor Green
} finally { Pop-Location }

$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
$version = (& (Join-Path $env:JAVA_HOME 'bin\javac.exe') -version 2>&1 | Out-String).Trim()
if ($version -notmatch '^javac 21(\.|$)') { throw "Exigido JDK 21: $version" }
$running = (& java --version | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $running -notmatch '(?m)^(openjdk|java) 21(\.|\s|$)') {
    throw "Gradle precisa executar com Java 21: $running"
}
Write-Host "Compilando os $($names.Count) JARs..." -ForegroundColor Yellow
Push-Location $root
try {
    & $gradle assembleTestPack --no-daemon
    if ($LASTEXITCODE -ne 0) { throw 'BUILD FALHOU. Nenhum JAR da instancia foi alterado.' }
} finally { Pop-Location }

$files = @()
foreach ($name in $names) {
    $found = @(Get-ChildItem $pack -Filter "$name-*.jar" -File |
        Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' })
    if ($found.Count -ne 1 -or $found[0].Length -le 0) {
        throw "BUILD INCOMPLETO: esperado 1 JAR para $name, encontrados $($found.Count)."
    }
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
$allNames = @($names) + @($obsolete)
$old = @()
foreach ($name in $allNames) {
    $old += @(Get-ChildItem $mods -Filter '*.jar' -File -ErrorAction Stop |
        Where-Object { $_.Name -match ('^' + [regex]::Escape($name) + '(-.*)?\.jar
$backup = Join-Path (Split-Path $mods -Parent) ('lux-mods-backup\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
if ($old.Count -gt 0) {
    New-Item -ItemType Directory -Force -Path $backup | Out-Null
    foreach ($file in $old) { Copy-Item $file.FullName -Destination $backup -Force }
}
try {
    foreach ($file in $old) { Remove-Item $file.FullName -Force }
    foreach ($file in $files) {
        Copy-Item $file.FullName -Destination $mods -Force
        $actual = Get-FileHash (Join-Path $mods $file.Name) -Algorithm SHA256
        $expected = Get-FileHash $file.FullName -Algorithm SHA256
        if ($actual.Hash -ne $expected.Hash) { throw "HASH INCORRETO: $($file.Name)" }
        Write-Host ("OK INSTALADO: " + $file.Name + "  sha256=" + $actual.Hash.Substring(0,12)) -ForegroundColor Green
    }
    foreach ($name in $obsolete) {
        if (@(Get-ChildItem $mods -Filter '*.jar' -File |
            Where-Object { $_.Name -match ('^' + [regex]::Escape($name) + '(-.*)?\.jar
            throw "JAR ANTIGO AINDA PRESENTE: $name"
        }
    }
} catch {
    foreach ($name in $allNames) {
        Get-ChildItem $mods -Filter '*.jar' -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match ('^' + [regex]::Escape($name) + '(-.*)?\.jar
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
) })
}
$backup = Join-Path (Split-Path $mods -Parent) ('lux-mods-backup\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
if ($old.Count -gt 0) {
    New-Item -ItemType Directory -Force -Path $backup | Out-Null
    foreach ($file in $old) { Copy-Item $file.FullName -Destination $backup -Force }
}
try {
    foreach ($file in $old) { Remove-Item $file.FullName -Force }
    foreach ($file in $files) {
        Copy-Item $file.FullName -Destination $mods -Force
        $actual = Get-FileHash (Join-Path $mods $file.Name) -Algorithm SHA256
        $expected = Get-FileHash $file.FullName -Algorithm SHA256
        if ($actual.Hash -ne $expected.Hash) { throw "HASH INCORRETO: $($file.Name)" }
        Write-Host ("OK INSTALADO: " + $file.Name + "  sha256=" + $actual.Hash.Substring(0,12)) -ForegroundColor Green
    }
    foreach ($name in $obsolete) {
        if (@(Get-ChildItem $mods -Filter "$name-*.jar" -File).Count -ne 0) {
            throw "JAR ANTIGO AINDA PRESENTE: $name"
        }
    }
} catch {
    foreach ($name in $allNames) {
        Get-ChildItem $mods -Filter "$name-*.jar" -File -ErrorAction SilentlyContinue |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
) }).Count -ne 0) {
            throw "JAR ANTIGO AINDA PRESENTE: $name"
        }
    }
} catch {
    foreach ($name in $allNames) {
        Get-ChildItem $mods -Filter "$name-*.jar" -File -ErrorAction SilentlyContinue |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
) })
}
$backup = Join-Path (Split-Path $mods -Parent) ('lux-mods-backup\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
if ($old.Count -gt 0) {
    New-Item -ItemType Directory -Force -Path $backup | Out-Null
    foreach ($file in $old) { Copy-Item $file.FullName -Destination $backup -Force }
}
try {
    foreach ($file in $old) { Remove-Item $file.FullName -Force }
    foreach ($file in $files) {
        Copy-Item $file.FullName -Destination $mods -Force
        $actual = Get-FileHash (Join-Path $mods $file.Name) -Algorithm SHA256
        $expected = Get-FileHash $file.FullName -Algorithm SHA256
        if ($actual.Hash -ne $expected.Hash) { throw "HASH INCORRETO: $($file.Name)" }
        Write-Host ("OK INSTALADO: " + $file.Name + "  sha256=" + $actual.Hash.Substring(0,12)) -ForegroundColor Green
    }
    foreach ($name in $obsolete) {
        if (@(Get-ChildItem $mods -Filter "$name-*.jar" -File).Count -ne 0) {
            throw "JAR ANTIGO AINDA PRESENTE: $name"
        }
    }
} catch {
    foreach ($name in $allNames) {
        Get-ChildItem $mods -Filter "$name-*.jar" -File -ErrorAction SilentlyContinue |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
) } |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
) })
}
$backup = Join-Path (Split-Path $mods -Parent) ('lux-mods-backup\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
if ($old.Count -gt 0) {
    New-Item -ItemType Directory -Force -Path $backup | Out-Null
    foreach ($file in $old) { Copy-Item $file.FullName -Destination $backup -Force }
}
try {
    foreach ($file in $old) { Remove-Item $file.FullName -Force }
    foreach ($file in $files) {
        Copy-Item $file.FullName -Destination $mods -Force
        $actual = Get-FileHash (Join-Path $mods $file.Name) -Algorithm SHA256
        $expected = Get-FileHash $file.FullName -Algorithm SHA256
        if ($actual.Hash -ne $expected.Hash) { throw "HASH INCORRETO: $($file.Name)" }
        Write-Host ("OK INSTALADO: " + $file.Name + "  sha256=" + $actual.Hash.Substring(0,12)) -ForegroundColor Green
    }
    foreach ($name in $obsolete) {
        if (@(Get-ChildItem $mods -Filter "$name-*.jar" -File).Count -ne 0) {
            throw "JAR ANTIGO AINDA PRESENTE: $name"
        }
    }
} catch {
    foreach ($name in $allNames) {
        Get-ChildItem $mods -Filter "$name-*.jar" -File -ErrorAction SilentlyContinue |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
) }).Count -ne 0) {
            throw "JAR ANTIGO AINDA PRESENTE: $name"
        }
    }
} catch {
    foreach ($name in $allNames) {
        Get-ChildItem $mods -Filter "$name-*.jar" -File -ErrorAction SilentlyContinue |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
) })
}
$backup = Join-Path (Split-Path $mods -Parent) ('lux-mods-backup\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
if ($old.Count -gt 0) {
    New-Item -ItemType Directory -Force -Path $backup | Out-Null
    foreach ($file in $old) { Copy-Item $file.FullName -Destination $backup -Force }
}
try {
    foreach ($file in $old) { Remove-Item $file.FullName -Force }
    foreach ($file in $files) {
        Copy-Item $file.FullName -Destination $mods -Force
        $actual = Get-FileHash (Join-Path $mods $file.Name) -Algorithm SHA256
        $expected = Get-FileHash $file.FullName -Algorithm SHA256
        if ($actual.Hash -ne $expected.Hash) { throw "HASH INCORRETO: $($file.Name)" }
        Write-Host ("OK INSTALADO: " + $file.Name + "  sha256=" + $actual.Hash.Substring(0,12)) -ForegroundColor Green
    }
    foreach ($name in $obsolete) {
        if (@(Get-ChildItem $mods -Filter "$name-*.jar" -File).Count -ne 0) {
            throw "JAR ANTIGO AINDA PRESENTE: $name"
        }
    }
} catch {
    foreach ($name in $allNames) {
        Get-ChildItem $mods -Filter "$name-*.jar" -File -ErrorAction SilentlyContinue |
            Remove-Item -Force -ErrorAction SilentlyContinue
    }
    if ($old.Count -gt 0) {
        Get-ChildItem $backup -Filter '*.jar' -File | ForEach-Object {
            Copy-Item $_.FullName -Destination $mods -Force
        }
    }
    throw
}
Write-Host "INSTALACAO VERIFICADA NA INSTANCIA $Instance! Codigo main @$commit" -ForegroundColor Green
Write-Host 'Lux Atlas antigo removido. Compass, Quests e Upgrades separados.' -ForegroundColor Cyan
explorer.exe $mods
