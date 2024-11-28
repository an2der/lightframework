package com.lightframework.util.os;

import com.lightframework.util.shell.CommandExecutor;
import com.lightframework.util.shell.CommandLine;
import com.lightframework.util.shell.CommandResult;

public class WindowsUtil {

    private WindowsUtil(){}

    /**
     * 是否登录到系统桌面
     * @return
     */
    public static boolean isLoginToDesktop(){
        CommandLine commandLine = CommandLine.build("tasklist 2>nul | findstr explorer.exe 1>nul 2>nul&&echo 1 || echo 0");
        CommandResult result = CommandExecutor.exec(commandLine);
        if (result.isSuccess()) {
            return result.getContent().trim().equals("1");
        }
        return false;
    }

}
