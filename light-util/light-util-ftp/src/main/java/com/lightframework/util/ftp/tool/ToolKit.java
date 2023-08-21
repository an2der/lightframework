package com.lightframework.util.ftp.tool;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * @author yg
 * @date 2023/8/21 15:58
 * @version 1.0
 */
public class ToolKit {
    /**
     * 路径如果不存在则自动创建
     **/
    public static void isChartPathExist(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String clearWindowsFileNameSpecialCharacter(String name){
        return name.replaceAll("[\\\\\\/:\\*\\?\"<>\\|]","#");
    }

    public static String clearWindowsFilePathSpecialCharacter(String name){
        return name.replaceAll("[\\*\\?\"<>\\|]","#");
    }


    public static boolean isAbsolutePath(String path) {
        if ((System.getProperty("os.name").toLowerCase().contains("windows") && path.indexOf(":") > 0) || (System.getProperty("os.name").toLowerCase().contains("linux") && path.startsWith("/"))) {
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
                            FileUtils.deleteDirectory(file1);
                        }
                    }else {
                        file1.delete();
                    }
                }
            }
        }
    }

    public static String extractIoExceptionMsg(IOException e){
        String message = e.getMessage();
        Pattern pattern = Pattern.compile("\\(.+\\)$");
        Matcher matcher = pattern.matcher(message);
        String result = "文件被占用，无法访问";
        if (matcher.find()) {
            result = matcher.group().replaceAll("(\\(|\\)|。)", "");
        }
        return result;
    }

    public static String getNetFileSizeDescription(long bytes) {
        if (bytes == 0) return "0B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(bytes / Math.pow(1024, digitGroups)) + "" + units[digitGroups];
    }
}
