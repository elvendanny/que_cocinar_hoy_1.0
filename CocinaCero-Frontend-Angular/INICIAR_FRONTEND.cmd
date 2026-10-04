@echo off
setlocal
cd /d "%~dp0"
title Cocina Cero - Angular 4200

echo ============================================
echo   COCINA CERO - FRONTEND ANGULAR
echo ============================================
echo.
if not exist node_modules (
  echo No existe node_modules.
  echo Ejecuta primero INSTALAR_Y_COMPILAR.cmd
  pause
  exit /b 1
)
echo Comprueba que Auth 8081 y Cocina 8082 esten iniciados.
echo La web abrira en http://127.0.0.1:4200
echo Para detener Angular usa Ctrl+C.
echo.
call npm.cmd start
pause
