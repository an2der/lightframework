package com.lightframework.util.file;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*** 文件工具
 * @author yg
 * @date 2023/11/7 11:41
 * @version 1.0
 */
public class FileUtil {
    /**
     * 转换文件大小单位
     * @param bytes
     * @return
     */
    public static String getNetFileSizeDescription(long bytes) {
        if (bytes == 0) return "0B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(bytes / Math.pow(1024, digitGroups)) + "" + units[digitGroups];
    }

    /**
     * 提取IOException中关键异常说明信息
     * @param e
     * @return
     */
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
}
