#!/bin/bash

## java env 必须指定java路径，否则systemctl无法使用
export JAVA_HOME=/usr/bin/java/jdk1.8.0_291
export JRE_HOME=$JAVA_HOME/jre

## service name
APP_NAME=${appName}

cd `dirname $0`
SERVICE_DIR=`pwd`
SERVICE_NAME=$APP_NAME
JAR_NAME=$SERVICE_NAME\.jar
PID=$SERVICE_NAME\.pid

PIDS=`ps -ef | grep -w "$JAR_NAME" | grep -v "grep" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
     echo "ERROR: The $SERVICE_NAME already started!"
     echo "PID: $PIDS"
     exit 1
fi

echo -e "Starting the $SERVICE_NAME ...\c"
nohup $JAVA_HOME/bin/java -jar -Xmx2g -Xms2g -Xmn1500m -XX:PermSize=100M -XX:MaxPermSize=200M -Xss256K -XX:+DisableExplicitGC -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=4 -XX:+CMSClassUnloadingEnabled -XX:LargePageSizeInBytes=128M -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=55 -XX:+PrintClassHistogram -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Dlogging.config=file:./config/log4j2.xml $JAR_NAME >/dev/null 2>&1 &

echo "OK!"
PIDS=`ps -ef | grep $SERVICE_NAME | grep -v "grep" | awk '{print $2}'`
sleep 1
echo "PID: $PIDS"
