@echo off
::%1 mshta vbscript:createobject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",0)(window.close)&&exit  ::管理员权限运行
%1 mshta vbscript:createobject("wscript.shell").run("cmd.exe /c %~s0 ::",0)(window.close)&&exit
set appName=${appName}
set jarName=%appName%.jar
wmic process where "commandline like '%%-jar%%%jarName%%%'" call terminate