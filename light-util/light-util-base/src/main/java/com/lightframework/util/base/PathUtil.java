package com.lightframework.util.base;

import java.io.File;

public class PathUtil {

    public static String getRealRootPath(Class clazz){
        String rootPath = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getFile()).getAbsolutePath();
        return rootPath.substring(0, rootPath.lastIndexOf(File.separator));
    }

    public static boolean isAbsolutePath(String path) {
        if ((System.getProperty("os.name").toLowerCase().contains("windows") && path.indexOf(":") > 0) || (System.getProperty("os.name").toLowerCase().contains("linux") && path.startsWith("/"))) {
            return true;
        }
        return false;
    }

    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
