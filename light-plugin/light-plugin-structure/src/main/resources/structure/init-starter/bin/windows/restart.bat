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
if "%2" == "serviceRestart" (
    goto SERVICE_RESTART
) else (
    for /f "delims=" %%i in ('sc query ${package.name} ^| findstr /I "STATE"') do set SERVICE_STATUS=%%i
    if defined SERVICE_STATUS (
        goto SERVICE_HANDLE
    ) else (
        goto RESTART
    )
)
exit

:SERVICE_HANDLE
:: 判断是否是管理员权限运行
net session >nul 2>&1
if %errorLevel% == 0 (
    goto SERVICE_RESTART
) else (
    goto SERVICE_RESTART_AS_ADMIN
)
exit

:SERVICE_RESTART
echo Service stopping...
nssm.exe stop ${package.name} 1>nul 2>nul
:: 等待服务停止
ping 127.0.0.1 -n 3 -w 1000 >nul
echo Service starting...
nssm.exe start ${package.name}
exit

:SERVICE_RESTART_AS_ADMIN
mshta vbscript:createobject("Shell.Application").ShellExecute("""%~f0""","!visible! serviceRestart","","runas",!visible!)(window.close)
exit

:RESTART
cmd /c shutdown.bat
cmd /c startup.bat

endlocal