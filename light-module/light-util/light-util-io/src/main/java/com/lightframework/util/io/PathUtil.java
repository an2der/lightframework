package com.lightframework.util.io;

import cn.hutool.core.io.FileUtil;
import com.lightframework.util.os.OSUtil;
import com.lightframework.util.project.ProjectUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;

/***
 * @author yg
 * @date 2023/8/21 15:58
 * @version 1.0
 */
public class PathUtil {

    private PathUtil(){}

    public static String toAbsolutePath(String path){
        return Paths.get(ProjectUtil.getProjectRootPath(), path).toString();
    }

    /**
     * 清除windows文件名中特殊字符
     * @param name
     * @return
     */
    public static String clearWindowsFileNameSpecialCharacter(String name){
        return name.replaceAll("[\\\\\\/:\\*\\?\"<>\\|]","#");
    }

    /**
     * 清除windows路径中特殊字符
     * @param name
     * @return
     */
    public static String clearWindowsFilePathSpecialCharacter(String name){
        return name.replaceAll("[\\*\\?\"<>\\|]","#");
    }

    /**
     * 验证是否是有效绝对路径
     * @param path
     * @return
     */
    public static boolean isAbsolutePath(String path) {
        if ((OSUtil.isWindows() && path.indexOf(":") > 0) || path.startsWith("/")) {
            return true;
        }
        return false;
    }

    /**
     * 删除linux中同名的文件
     * @param targetFile
     */
    public static void clearDuplicateNameFiles(File targetFile,boolean isDir) throws IOException {
        if(targetFile.getParentFile().exists()){
            File [] duplicateName = targetFile.getParentFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.equalsIgnoreCase(targetFile.getName())&&!name.equals(targetFile.getName());
                }
            });
            if(duplicateName.length > 0){
                for (File file1 : duplicateName) {
                    if(file1.isDirectory()){
                        if(!isDir || !file1.renameTo(targetFile)){
                            FileUtil.del(file1);
                        }
                    }else {
                        file1.delete();
                    }
                }
            }
        }
    }

}
