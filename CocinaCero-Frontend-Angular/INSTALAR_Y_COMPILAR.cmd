@echo off
setlocal
cd /d "%~dp0"
title Cocina Cero - Instalar y compilar Angular

echo ============================================
echo   COCINA CERO - INSTALACION FRONTEND
echo ============================================
echo.
where node >nul 2>nul || (echo ERROR: Node.js no esta instalado o no esta en PATH.& pause & exit /b 1)
where npm.cmd >nul 2>nul || (echo ERROR: npm no esta disponible.& pause & exit /b 1)

echo Version de Node:
node -v
echo Version de npm:
call npm.cmd -v
echo.

echo [1/2] Instalando dependencias Angular...
call npm.cmd install
if errorlevel 1 goto :error

echo.
echo [2/2] Compilando el proyecto...
call npm.cmd run build
if errorlevel 1 goto :error

echo.
echo ============================================
echo   BUILD CORRECTO - FRONTEND LISTO
echo ============================================
echo Ahora ejecuta INICIAR_FRONTEND.cmd
pause
exit /b 0

:error
echo.
echo ERROR: revisa el mensaje mostrado arriba.
pause
exit /b 1
