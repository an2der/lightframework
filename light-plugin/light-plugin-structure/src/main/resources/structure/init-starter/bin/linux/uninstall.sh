#!/bin/sh

NAME=${package.name}
systemctl_list=$(systemctl list-unit-files --no-pager --type=service --all)
if echo "$systemctl_list" | grep -q "$NAME"; then
    systemctl stop $NAME.service && systemctl disable $NAME.service
    rm -f /etc/systemd/system/$NAME.service
    systemctl daemon-reload
    echo -e "\033[32m $NAME 服务卸载完成\033[0m"
else
    echo -e "\033[32m $NAME 服务不存在，无需卸载\033[0m"
fi
