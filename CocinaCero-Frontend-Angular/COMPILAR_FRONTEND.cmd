@echo off
setlocal
cd /d "%~dp0"
title Cocina Cero - Compilar Angular
call npm.cmd run build
if errorlevel 1 (
  echo.
  echo BUILD CON ERRORES.
) else (
  echo.
  echo BUILD CORRECTO.
)
pause
