#!/bin/bash

cd $(dirname $0)
BASE_DIR=$(pwd)

NAME=${package.name}
if systemctl status "$NAME" >/dev/null 2>&1; then
    echo -e "\033[32m 服务 $NAME 服务已存在\033[0m"
else
    \cp -r ./$NAME.service  /etc/systemd/system/
    sed -i "s/^ExecStart=.*/ExecStart=$BASE_DIR\/startup.sh/g" /etc/systemd/system/$NAME.service
    sed -i "s/^ExecStop=.*/ExecStop=$BASE_DIR\/shutdown.sh/g" /etc/systemd/system/$NAME.service
    chmod +x /etc/systemd/system/$NAME.service
    systemctl daemon-reload
    systemctl enable $NAME
    echo -e "\033[32m $NAME 服务创建完成\033[0m"
fi