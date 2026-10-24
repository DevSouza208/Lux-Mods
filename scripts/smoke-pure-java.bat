@echo off
setlocal
cd /d "%~dp0.."
set "LUX_TMP=%TEMP%\luxmods-smoke-%RANDOM%-%RANDOM%"
mkdir "%LUX_TMP%" || exit /b 1
javac -encoding UTF-8 -d "%LUX_TMP%" ^
 modules\lux-engine\src\main\java\dev\luxmods\engine\api\LuxServiceKey.java ^
 modules\lux-engine\src\main\java\dev\luxmods\engine\api\LuxServiceRegistry.java ^
 modules\lux-discovery\src\main\java\dev\luxmods\discovery\DiscoveryDebouncer.java ^
 scripts\src\LuxSmokeMain.java
if errorlevel 1 (
  rmdir /s /q "%LUX_TMP%"
  exit /b 1
)
java -cp "%LUX_TMP%" LuxSmokeMain
set "LUX_EXIT=%ERRORLEVEL%"
rmdir /s /q "%LUX_TMP%"
exit /b %LUX_EXIT%
