@echo off
setlocal
title LUX MODS - Inventario para Playtest
cd /d "%~dp0.."
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "scripts\reunir-evidencias.ps1"
echo.
echo O checklist esta em docs\30-CHECKLIST-DE-TESTES.md
echo Nenhum teste de gameplay foi executado automaticamente.
pause
