package com.lightframework.util.os;

import com.lightframework.util.shell.ShellUtil;

public class WindowsUtil {

    private WindowsUtil(){}

    public static boolean isLoginToDesktop(){
        ShellUtil.ShellResult result = ShellUtil.execWindowsCommandWithResult("tasklist 2>nul | findstr explorer.exe 1>nul 2>nul&&echo 1 || echo 0");
        if (result.isSuccess()) {
            return result.getContent().trim().equals("1");
        }
        return false;
    }

}
