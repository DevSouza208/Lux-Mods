# Local-only inventory for the deferred Lux Mods integrated playtest.
# Works with PowerShell 5.1; no uploads or mutations to Minecraft/saves.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$mods = Join-Path $env:APPDATA '.sklauncher\instances\teste\mods'
$target = Join-Path $root 'build\lux-playtest'
New-Item -Path $target -ItemType Directory -Force | Out-Null
$out = Join-Path $target 'inventario-local.txt'
$expected = @('lux-engine-', 'lux-discovery-', 'lux-atlas-', 'lux-hud-', 'lux-menu-')
$lines = @(
  'LUX MODS - INVENTARIO LOCAL',
  ('Data/hora: ' + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss')),
  ('Pasta mods: ' + $mods),
  'NENHUM DADO E ENVIADO AUTOMATICAMENTE.',
  ''
)
if (-not (Test-Path $mods -PathType Container)) {
  $lines += 'ERRO: pasta de mods nao encontrada. Crie/abra a instancia teste antes.'
} else {
  $files = @(Get-ChildItem -Path $mods -Filter 'lux-*.jar' -File | Sort-Object Name)
  foreach ($prefix in $expected) {
    $matching = @($files | Where-Object { $_.Name.StartsWith($prefix) })
    if ($matching.Count -eq 1) {
      $file = $matching[0]
      $hash = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash
      $lines += ('OK: ' + $file.Name + ' (' + $file.Length + ' bytes) SHA256=' + $hash)
    } elseif ($matching.Count -eq 0) {
      $lines += ('PENDENTE: ' + $prefix + '*.jar nao encontrado.')
    } else {
      $lines += ('ATENCAO: ' + $matching.Count + ' JARs encontrados para ' + $prefix + ' (versoes duplicadas).')
    }
  }
  $lines += ''
  $lines += ('Total de JARs Lux: ' + $files.Count)
}
try {
  $head = (& git -C $root rev-parse --short HEAD 2>$null)
  if ($LASTEXITCODE -eq 0) { $lines += ('Commit local: ' + $head) }
} catch { $lines += 'Commit local indisponivel.' }
$lines += ''
$lines += 'Checklist: docs\30-CHECKLIST-DE-TESTES.md'
$lines += 'Este relatorio NAO prova que os mods funcionam em gameplay.'
$lines | Out-File -FilePath $out -Encoding UTF8
Write-Host "Inventario salvo: $out" -ForegroundColor Green
Get-Content -Path $out
