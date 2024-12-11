@echo off
::%1 mshta vbscript:createobject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",0)(window.close)&&exit ::管理员权限运行
%1 mshta vbscript:createobject("wscript.shell").run("cmd.exe /c %~s0 ::",0)(window.close)&&exit
cd /d %~dp0
set jarName=${package.name}.jar

setlocal enabledelayedexpansion
for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATE=%%i

if defined SERVICE_STATE (
    echo !SERVICE_STATE! | findstr /I "RUNNING" 1>nul 2>nul&&set STARTED=1
    if defined STARTED (
        echo "Service already started..."
    ) else (
        ${package.name}.exe start
    )
) else (
    wmic process where "commandline like '%%-jar%%%jarName%%%'" get commandline 2>nul | findstr /V "wmic" | findstr /I "java" 1>nul 2>nul&&set STARTED=1
    if defined STARTED (
        echo "Service already started..."
    ) else (
        "${package.windows.javaPath}" ${package.vmOptions} -jar %jarName%
    )
)

endlocal