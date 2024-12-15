@echo off
cd /d %~dp0
for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATUS=%%i
if defined SERVICE_STATUS (
    echo Service already installed.
) else (
    nssm.exe install ${package.name} %CD%\startup.bat service&&echo Service install successful.
)
pause