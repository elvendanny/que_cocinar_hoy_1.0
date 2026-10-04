@echo off
cd /d "%~dp0"
powershell -NoProfile -File scripts\Empacar.ps1
pause
