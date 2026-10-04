@echo off
cd /d "%~dp0"
java scripts/ImportarConfiguracion.java
if errorlevel 1 (
  echo Importacion detenida. Revisa el mensaje anterior.
  pause
  exit /b 1
)
pause
