package com.lightframework.util.os;

public class OSUtil {
    private OSUtil(){}

    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean isUnix(){
        return !isWindows() && !isMacOS();
    }

    public static boolean isMacOS(){
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}
