@echo off
setlocal
title LUX BOOT - Previa visual
if not exist "%~dp0index.html" (
  echo ERRO: index.html nao foi encontrado nesta pasta.
  pause
  exit /b 1
)
start "" "%~dp0index.html"
