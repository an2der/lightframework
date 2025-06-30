#!/bin/bash

## java env 必须指定java路径，否则systemctl无法使用
export JAVA_PATH=${package.linux.javaPath}

## service name
APP_NAME=${package.name}

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
nohup $JAVA_PATH ${package.vmOptions} -jar $JAR_NAME >/dev/null 2>&1 &

echo "OK!"
PIDS=`ps -ef | grep $SERVICE_NAME | grep -v "grep" | awk '{print $2}'`
sleep 1
echo "PID: $PIDS"
