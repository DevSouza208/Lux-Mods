# Lux Boot — reversible dark startup for SKlauncher TESTE only.
# Default Preview: no modifications. This is NOT a custom graphics provider.
[CmdletBinding()]
param([ValidateSet('Preview','Apply','Restore')][string]$Action = 'Preview')
$ErrorActionPreference = 'Stop'
$instance = Join-Path $env:APPDATA '.sklauncher\instances\teste'
$fml = Join-Path $instance 'config\fml.toml'
$options = Join-Path $instance 'options.txt'
$backups = Join-Path $instance 'lux-boot-backup'
function Read-Text($path) {
    return [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
}
function Write-Text($path, $value) {
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($path, $value, $utf8)
}
if (-not (Test-Path $instance -PathType Container)) {
    throw "Instancia de teste nao encontrada: $instance"
}
if ((Split-Path $instance -Leaf) -ne 'teste') {
    throw 'Seguranca: esta ferramenta atua exclusivamente na instancia teste.'
}
if ($Action -eq 'Restore') {
    $previous = @(Get-ChildItem -Path $backups -Directory -ErrorAction SilentlyContinue |
        Where-Object { (Test-Path (Join-Path $_.FullName 'fml.toml')) -and
                       (Test-Path (Join-Path $_.FullName 'options.txt')) } |
        Sort-Object Name -Descending | Select-Object -First 1)
    if ($previous.Count -ne 1) {
        throw 'Backup ausente. Nenhuma alteracao realizada.'
    }
    Copy-Item -LiteralPath (Join-Path $previous[0].FullName 'fml.toml') -Destination $fml -Force
    Copy-Item -LiteralPath (Join-Path $previous[0].FullName 'options.txt') -Destination $options -Force
    Write-Host 'Configuracoes originais restauradas. Reinicie o Minecraft.' -ForegroundColor Green
    exit 0
}
if (-not (Test-Path $fml -PathType Leaf) -or -not (Test-Path $options -PathType Leaf)) {
    throw 'fml.toml ou options.txt nao encontrado. Abra a instancia uma vez antes de usar o tema.'
}
$oldFml = Read-Text $fml
$oldOptions = Read-Text $options
$pat = '(?m)^[ \t]*earlyWindowControl[ \t]*=[ \t]*(?:true|false)[ \t]*(?:#.*)?$'
$hits = [regex]::Matches($oldFml, $pat)
if ($hits.Count -ne 1) {
    throw "Configuracao FML desconhecida: esperado um earlyWindowControl, encontrado $($hits.Count). Abortado."
}
$newFml = [regex]::Replace($oldFml, $pat, 'earlyWindowControl = false')
$optPat = '(?m)^darkMojangStudiosBackground:(?:true|false)[ \t]*$'
$optHits = [regex]::Matches($oldOptions, $optPat)
if ($optHits.Count -gt 1) {
    throw 'Configuracao Mojang duplicada em options.txt. Abortado.'
}
if ($optHits.Count -eq 1) {
    $newOptions = [regex]::Replace($oldOptions, $optPat, 'darkMojangStudiosBackground:true')
} else {
    $ending = [Environment]::NewLine
    if ($oldOptions.EndsWith([char]10)) { $ending = '' }
    $newOptions = $oldOptions + $ending + 'darkMojangStudiosBackground:true' + [Environment]::NewLine
}
Write-Host '=== LUX BOOT / MODO EXPERIMENTAL REVERSIVEL ===' -ForegroundColor Magenta
Write-Host "Instancia: $instance"
Write-Host 'Desabilita a janela vermelha antecipada FML; escurece somente a tela Mojang posterior.'
Write-Warning 'NAO e um tema Lux completo: barras, estatisticas e a raposa da janela FML deixam de aparecer.'
if ($Action -eq 'Preview') {
    Write-Host 'PREVIEW: nada foi alterado.' -ForegroundColor Green
    exit 0
}
if ($newFml -ceq $oldFml -and $newOptions -ceq $oldOptions) {
    Write-Host 'Opcoes ja estavam aplicadas. Nada modificado.' -ForegroundColor Green
    exit 0
}
$folder = Join-Path $backups (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
New-Item -Path $folder -ItemType Directory -Force | Out-Null
Copy-Item -LiteralPath $fml -Destination (Join-Path $folder 'fml.toml') -Force
Copy-Item -LiteralPath $options -Destination (Join-Path $folder 'options.txt') -Force
try {
    if ($newFml -cne $oldFml) { Write-Text $fml $newFml }
    if ($newOptions -cne $oldOptions) { Write-Text $options $newOptions }
} catch {
    Copy-Item (Join-Path $folder 'fml.toml') -Destination $fml -Force
    Copy-Item (Join-Path $folder 'options.txt') -Destination $options -Force
    throw 'Falha na gravacao. Arquivos originais restaurados.'
}
Write-Host "Aplicado somente na instancia teste. Backup: $folder" -ForegroundColor Green
Write-Host 'Para desfazer: -Action Restore. Reinicie o Minecraft.'
