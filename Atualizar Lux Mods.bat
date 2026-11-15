@echo off
setlocal
rem Execute o instalador versionado na mesma pasta deste arquivo.
set "ROOT=%~dp0"
if not exist "%ROOT%scripts\install-to-sklauncher.ps1" (
  echo ERRO: scripts\install-to-sklauncher.ps1 nao encontrado ao lado deste atalho.
  echo Repositorio esperado: %ROOT%
  pause
  exit /b 1
)
echo Atualizador Lux Mods - repositorio: %ROOT%
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%ROOT%scripts\install-to-sklauncher.ps1" -Instance lux-mods
set "RESULT=%ERRORLEVEL%"
if not "%RESULT%"=="0" echo ERRO: instalacao interrompida, codigo %RESULT%.
pause
exit /b %RESULT%
