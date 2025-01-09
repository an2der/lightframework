@echo off

cd /d %~dp0
set JAVA_DIR=%CD%

net session >nul 2>&1
if %errorLevel% == 0 (
    setx JAVA_HOME "%JAVA_DIR%" /m
    setx CLASSPATH ".;%%JAVA_HOME%%\lib\tools.jar;%%JAVA_HOME%%\lib\dt.jar;" /m
    echo %PATH% | findstr /I /C:"%JAVA_HOME%\bin">nul 2>nul&&set EXISTED=1
    if not defined EXISTED (
        setx PATH "%PATH%;%%JAVA_HOME%%\bin" /m
    )

    "%JAVA_DIR%\bin\java.exe" -version
) else (
    goto RUN_AS_ADMIN
)
pause
exit

:RUN_AS_ADMIN
mshta vbscript:createobject("Shell.Application").ShellExecute("""%~f0""","","","runas",1)(window.close)
exit