@echo off

cd /d %~dp0
set jarName=${package.name}.jar
if "%1" == "serviceStop" (
    nssm.exe stop ${package.name} 1>nul 2>nul
) else (
    for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATUS=%%i
    if defined SERVICE_STATUS (
        echo %SERVICE_STATUS% | findstr /I "STOPPED" 1>nul 2>nul&&set STATUS_STOPPED=1
        if not defined STATUS_STOPPED (
            goto SERVICE_STOP
        )
    ) else (
        wmic process where "commandline like '%%-jar%%%jarName%%%'" call terminate 1>nul 2>nul
    )
)
exit

:SERVICE_STOP
mshta vbscript:createobject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 serviceStop","","runas",1)(window.close)
exit