@echo off
cd /d "%~dp0"
java scripts\Configurar.java
if errorlevel 1 goto error
pause
exit /b 0
:error
echo No se completo la configuracion. Revisa el mensaje anterior.
pause
exit /b 1
