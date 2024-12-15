@echo off
cd /d %~dp0

if "%1" == "serviceRestart" (
    nssm.exe stop ${package.name}
    timeout /t 1 >nul
    nssm.exe start ${package.name}
) else (
    for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATUS=%%i
    if defined SERVICE_STATUS (
        goto SERVICE_RESTART
    ) else (
        goto RESTART
    )
)
exit

:SERVICE_RESTART
mshta vbscript:createobject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 serviceRestart","","runas",1)(window.close)
exit

:RESTART
cmd /c shutdown.bat
cmd /c startup.bat