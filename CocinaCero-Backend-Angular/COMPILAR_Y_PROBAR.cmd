@echo off
cd /d "%~dp0"
call mvnw.cmd clean package
if errorlevel 1 goto error
echo Compilacion y pruebas terminadas.
pause
exit /b 0
:error
echo La compilacion o las pruebas fallaron. Copia el error para revisarlo.
pause
exit /b 1
