@echo off
setlocal
cd /d "%~dp0.."
set "LUX_TMP=%TEMP%\luxmods-smoke-%RANDOM%-%RANDOM%"
mkdir "%LUX_TMP%" || exit /b 1
javac --release 21 -encoding UTF-8 -d "%LUX_TMP%" ^
 modules\lux-engine\src\main\java\dev\luxmods\engine\api\LuxServiceKey.java ^
 modules\lux-engine\src\main\java\dev\luxmods\engine\api\LuxServiceRegistry.java ^
 modules\lux-engine\src\main\java\dev\luxmods\engine\api\LuxEventType.java ^
 modules\lux-engine\src\main\java\dev\luxmods\engine\api\LuxEventBus.java ^
 modules\lux-discovery\src\main\java\dev\luxmods\discovery\DiscoveryDebouncer.java ^
 modules\lux-discovery\src\main\java\dev\luxmods\discovery\DiscoveryLedger.java ^
 modules\lux-discovery\src\main\java\dev\luxmods\discovery\LuxDiscoveryEvents.java ^
 modules\lux-atlas\src\main\java\dev\luxmods\atlas\CompassMath.java ^
 scripts\src\LuxCompassSmoke.java ^
 scripts\src\LuxSmokeMain.java ^
 scripts\src\LuxEventDepthSmoke.java
if errorlevel 1 (
  rmdir /s /q "%LUX_TMP%"
  exit /b 1
)
java -cp "%LUX_TMP%" LuxSmokeMain
if errorlevel 1 (
  rmdir /s /q "%LUX_TMP%"
  exit /b 1
)
java -cp "%LUX_TMP%" LuxEventDepthSmoke
if errorlevel 1 (
  rmdir /s /q "%LUX_TMP%"
  exit /b 1
)
java -cp "%LUX_TMP%" LuxCompassSmoke
set "LUX_EXIT=%ERRORLEVEL%"
rmdir /s /q "%LUX_TMP%"
exit /b %LUX_EXIT%
