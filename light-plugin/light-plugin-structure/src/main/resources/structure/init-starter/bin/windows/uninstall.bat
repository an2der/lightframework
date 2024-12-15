@echo off
%1 mshta vbscript:createobject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit ::管理员权限运行
cd /d %~dp0
for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATUS=%%i
if defined SERVICE_STATUS (
    echo %SERVICE_STATUS% | findstr /I "STOPPED" 1>nul 2>nul&&set STATUS_STOPPED=1
    if defined STATUS_STOPPED (
        echo Service current status is stopped.
    ) else (
        echo Service is stopping...
        nssm.exe stop ${package.name} 1>nul 2>nul
    )
    nssm.exe remove ${package.name} && exit
) else (
    echo Service not installed.
)
pause