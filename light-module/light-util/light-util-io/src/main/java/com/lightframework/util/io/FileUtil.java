package com.lightframework.util.io;

import java.io.File;

/**
 * @Author yg
 * @Date 2025/9/17 17:27
 */
public class FileUtil {

    private FileUtil(){}

    public static File getProjectFile(String path){
        File file = new File(PathUtil.toAbsolutePath(path));
        return file.exists()?file:null;
    }
}
