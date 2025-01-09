@echo off

set current_path=%CD%\
set base_path=%~dp0
cd /d %base_path%
setlocal enabledelayedexpansion
:: 判断是手动执行还是被其他程序调用执行
set visible=%1
if "!visible!" NEQ "1" (
	if "!visible!" NEQ "0" (
		if /I "%current_path%" EQU "%base_path%" (
			set visible=1
		) else if /I "%current_path%" EQU "C:\WINDOWS\system32\" (
			set visible=1
		) else (
			set visible=0
		)
	)
)
:: 手动执行则显示控制台窗口并展示提示信息，否则隐藏窗口
:: 隐藏窗口后不能存在pause，否则cmd进程无法关闭
if !visible! == 0 (
	set @REM=::
)

:: 脚本逻辑
:RUN
set jarName=${package.name}.jar
if "%2" == "service" (
    goto START
) else if "%2" == "serviceStartMode" (
    goto SERVICE_START
) else if "%2" == "commandStartMode" (
    goto START
) else (
    for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATUS=%%i
    if defined SERVICE_STATUS (
        goto SERVICE_HANDLE
    ) else (
        wmic process where "commandline like '%%-jar%%%jarName%%%'" get commandline 2>nul | findstr /V "wmic" | findstr /I "java" 1>nul 2>nul&&set STATUS_STARTED=1
        if defined STATUS_STARTED (
            echo Service already started
        ) else (
            goto COMMAND_START
        )
    )
)
%@REM% pause
exit

:SERVICE_HANDLE
:: 判断是否是管理员权限运行
net session >nul 2>&1
if %errorLevel% == 0 (
    goto SERVICE_START
) else (
    goto SERVICE_START_AS_ADMIN
)
exit

:SERVICE_START
nssm.exe start ${package.name}
%@REM% pause
exit

:SERVICE_START_AS_ADMIN
mshta vbscript:createobject("Shell.Application").ShellExecute("""%~f0""","!visible! serviceStartMode","","runas",!visible!)(window.close)
exit

:COMMAND_START
mshta vbscript:createobject("wscript.shell").run("cmd.exe /c %~s0 0 commandStartMode",0)(window.close)
exit

:START
"${package.windows.javaPath}" ${package.vmOptions} -jar %jarName%

endlocal