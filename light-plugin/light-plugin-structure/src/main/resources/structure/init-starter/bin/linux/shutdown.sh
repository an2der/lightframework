#!/bin/bash

## service name
APP_NAME=${package.name}

cd `dirname $0`
SERVICE_DIR=`pwd`
SERVICE_NAME=$APP_NAME
JAR_NAME=$SERVICE_NAME\.jar
PID=$SERVICE_NAME\.pid

PIDS=`ps -ef | grep -w "$JAR_NAME" | grep -v "grep" |awk '{print $2}'`
if [ -z "$PIDS" ]; then
    echo "ERROR: The $SERVICE_NAME does not started!"
    exit 1
fi

echo -e "Stopping the $SERVICE_NAME ...\c"
for PID in $PIDS ; do
    kill -9  $PID > /dev/null 2>&1
done

COUNT=0
while [ $COUNT -lt 1 ]; do    
    echo -e ".\c"
    sleep 1
    COUNT=1
    for PID in $PIDS ; do
        PID_EXIST=`ps -f -p $PID | grep $SERVICE_NAME`
        if [ -n "$PID_EXIST" ]; then
            COUNT=0
            break
        fi
    done
done

echo "OK!"
echo "PID: $PIDS"
