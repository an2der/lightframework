@echo off

cd /d %~dp0
set jarName=${package.name}.jar

if "%1" == "service" (
    goto START
) else if "%1" == "serviceStartMode" (
    nssm.exe start ${package.name}
) else if "%1" == "commandStartMode" (
    goto START
) else (
    for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATUS=%%i
    if defined SERVICE_STATUS (
        goto SERVICE_START
    ) else (
        wmic process where "commandline like '%%-jar%%%jarName%%%'" get commandline 2>nul | findstr /V "wmic" | findstr /I "java" 1>nul 2>nul&&set STATUS_STARTED=1
        if defined STATUS_STARTED (
            echo Service already started
        ) else (
            goto COMMAND_START
        )
    )
)
pause
exit

:SERVICE_START
mshta vbscript:createobject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 serviceStartMode","","runas",1)(window.close)
exit

:COMMAND_START
mshta vbscript:createobject("wscript.shell").run("cmd.exe /c %~s0 commandStartMode",0)(window.close)
exit

:START
"${package.windows.javaPath}" ${package.vmOptions} -jar %jarName%

