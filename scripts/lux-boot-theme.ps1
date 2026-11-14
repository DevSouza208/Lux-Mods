# Lux Boot v2 — FML 1.21.1 BLACK palette preserving progress indicators.
# Preview by default. ONLY the SKlauncher "teste" instance is modified.
[CmdletBinding()]
param([ValidateSet('Preview','Apply','Restore')][string]$Action='Preview')
$ErrorActionPreference='Stop'
$dir=Join-Path $env:APPDATA '.sklauncher\instances\teste'
$options=Join-Path $dir 'options.txt'
$fml=Join-Path $dir 'config\fml.toml'
$backups=Join-Path $dir 'lux-boot-backup'
$pattern='(?m)^darkMojangStudiosBackground:(?<state>true|false)(?<trail>[ \t]*)(?<cr>\r?)$'
function Read-Text($path) {[System.IO.File]::ReadAllText($path,[Text.Encoding]::UTF8)}
function Write-Text($path,$value) {
    $encoding=New-Object Text.UTF8Encoding($false)
    [IO.File]::WriteAllText($path,$value,$encoding)
}
if ((Split-Path $dir -Leaf) -cne 'teste' -or !(Test-Path $dir -PathType Container)) {
    throw "Instancia teste indisponivel: $dir"
}
if (!(Test-Path $options -PathType Leaf)) {throw "options.txt nao encontrado: $options"}
$current=Read-Text $options
$found=[regex]::Matches($current,$pattern)
if($found.Count -gt 1){throw 'Preferencia duplicada; abortado sem alteracoes.'}
$lf=[string][char]10
$crlf=[string][char]13+$lf
$eol=if($current.Contains($crlf)){$crlf}else{$lf}
$fmlControl='desconhecido'
if(Test-Path $fml -PathType Leaf){
    $control=[regex]::Matches((Read-Text $fml),'(?m)^[ \t]*earlyWindowControl[ \t]*=[ \t]*(true|false)[ \t]*(?:#.*)?\r?$')
    if($control.Count -gt 1){throw 'Configuracoes earlyWindowControl duplicadas; abortado.'}
    if($control.Count -eq 1){$fmlControl=$control[0].Groups[1].Value}
}
if($Action -eq 'Restore'){
    $last=@(Get-ChildItem $backups -Directory -ErrorAction SilentlyContinue |
        Where-Object {Test-Path (Join-Path $_.FullName 'boot-state.json')} |
        Sort-Object Name -Descending | Select-Object -First 1)
    if($last.Count -ne 1){throw 'Backup Lux Boot v2 nao encontrado.'}
    $state=Get-Content (Join-Path $last[0].FullName 'boot-state.json') -Raw | ConvertFrom-Json
    if($state.schema -ne 2 -or $state.instanceName -ne 'teste'){throw 'Backup invalido.'}
    if($state.originalPresent){
        $oldLine=[string]$state.originalLine
        if($found.Count -eq 0){
            $sep=if($current.EndsWith($lf)){''}else{$eol}
            $restored=$current+$sep+$oldLine+$eol
        }else{
            $restored=[regex]::Replace($current,$pattern,[Text.RegularExpressions.MatchEvaluator]{
                param($m)
                return $oldLine+$m.Groups['cr'].Value
            })
        }
    }else{
        $restored=[regex]::Replace($current,'(?m)^darkMojangStudiosBackground:(?:true|false)[ \t]*\r?\n?','')
    }
    if($restored -cne $current){Write-Text $options $restored}
    Write-Host 'Preferencia original restaurada, outras opcoes preservadas.' -ForegroundColor Green
    exit 0
}
$oldValue=if($found.Count -eq 0){'ausente'}else{$found[0].Groups['state'].Value}
Write-Host "LUX BOOT - instancia TESTE - earlyWindowControl=$fmlControl (inalterado)" -ForegroundColor Magenta
Write-Host "darkMojangStudiosBackground: $oldValue -> true (FML black com barras de progresso)"
Write-Warning 'Apenas fundo preto; tema grafite/roxo completo ainda nao implementado.'
if($Action -eq 'Preview'){Write-Host 'PREVIEW: nenhuma alteracao.';exit 0}
if($oldValue -eq 'true'){Write-Host 'Ja ativo; nenhuma alteracao.';exit 0}
$backup=Join-Path $backups (Get-Date -Format 'yyyyMMdd-HHmmss-fff')
New-Item $backup -ItemType Directory -Force | Out-Null
Copy-Item -LiteralPath $options -Destination (Join-Path $backup 'options.txt') -Force
if(Test-Path $fml -PathType Leaf){
    Copy-Item -LiteralPath $fml -Destination (Join-Path $backup 'fml.toml') -Force
}
$line=if($found.Count -eq 1){$found[0].Value.TrimEnd([char]13)}else{''}
$state=@{schema=2;instanceName='teste';originalPresent=($found.Count -eq 1);originalLine=$line}
Write-Text (Join-Path $backup 'boot-state.json') ($state | ConvertTo-Json)
if($found.Count -eq 1){
    $updated=[regex]::Replace($current,$pattern,[Text.RegularExpressions.MatchEvaluator]{
        param($m)
        return 'darkMojangStudiosBackground:true'+$m.Groups['trail'].Value+$m.Groups['cr'].Value
    })
}else{
    $sep=if($current.EndsWith($lf)){''}else{$eol}
    $updated=$current+$sep+'darkMojangStudiosBackground:true'+$eol
}
try{Write-Text $options $updated}
catch{
    Copy-Item (Join-Path $backup 'options.txt') -Destination $options -Force
    throw 'Falha na escrita; options.txt original recuperado.'
}
Write-Host "Aplicado a TESTE. Backup: $backup" -ForegroundColor Green
