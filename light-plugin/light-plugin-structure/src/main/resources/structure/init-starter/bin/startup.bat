@echo off
::%1 mshta vbscript:createobject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",0)(window.close)&&exit ::管理员权限运行
%1 mshta vbscript:createobject("wscript.shell").run("cmd.exe /c %~s0 ::",0)(window.close)&&exit
cd /d %~dp0
set jarName=${appName}.jar
wmic process where "commandline like '%%-jar%%%jarName%%%'" get commandline 2>nul | findstr /V "wmic" | findstr /I "java" 1>nul 2>nul&&set STARTED=1
if defined STARTED (
    echo "Service already started..."
) else (
    "C:\Program Files\Java\jdk1.8.0_281\bin\java.exe" -jar -Xmx2g -Xms2g -Xmn1500m -XX:PermSize=100M -XX:MaxPermSize=200M -Xss256K -XX:+DisableExplicitGC -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=4 -XX:+CMSClassUnloadingEnabled -XX:LargePageSizeInBytes=128M -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=55 -XX:+PrintClassHistogram -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Xloggc:logs/gc.log -Dlogging.config=file:./config/log4j2.xml %jarName%
)