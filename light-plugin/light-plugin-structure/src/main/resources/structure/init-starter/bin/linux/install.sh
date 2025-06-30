#!/bin/bash

cd $(dirname $0)
BASE_DIR=$(pwd)

NAME=${package.name}
systemctl_list=$(systemctl list-unit-files --no-pager --type=service --all)
if echo "$systemctl_list" | grep -q "$NAME"; then
    echo -e "\033[32m 服务 $NAME 服务已存在\033[0m"
else
    \cp -r ./$NAME.service  /etc/systemd/system/
    tmp=$(echo $BASE_DIR | sed 's/\//\\\//g')
    sed -i "s/^ExecStart=.*/ExecStart=$tmp\/startup.sh/g" /etc/systemd/system/$NAME.service
    sed -i "s/^ExecStop=.*/ExecStop=$tmp\/shutdown.sh/g" /etc/systemd/system/$NAME.service
    chmod +x /etc/systemd/system/$NAME.service
    systemctl daemon-reload
    systemctl enable $NAME
    echo -e "\033[32m $NAME 服务创建完成\033[0m"
fi