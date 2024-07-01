package com.lightframework.util.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/***
 * @author yg
 * @date 2022/5/23 15:50
 * @version 1.0
 */
public class ShellUtil {

    private ShellUtil(){}

    private static Logger log = LoggerFactory.getLogger(ShellUtil.class);

    public static void execWindowsScript(String src){
        ShellUtil.execScript("cmd /c ",src);
    }

    public static void execLinuxScript(String src){
        ShellUtil.execScript("sh ",src);
    }

    public static ShellResult execWindowsCommandWithResult(String command){
        return ShellUtil.execCommandWithResult("cmd /c ",command);
    }

    public static void execWindowsCommandWithOutResult(String command){
        ShellUtil.execCommandWithOutResult("cmd /c ",command);
    }

    public static void execWindowsCommandAsAdmin(String command){
        String cmd = "mshta vbscript:createobject(\"Shell.Application\").ShellExecute(\"cmd.exe\",\"/c "+command+"\",\"\",\"runas\",0)(window.close)";
        execWindowsCommandWithOutResult(cmd);
    }

    public static ShellResult execCommandWithResult(String commandPrefix,String command){
        Process process = null;
        ShellResult result = new ShellResult();
        try {
            log.info("开始执行命令["+command+"]");
            process = Runtime.getRuntime().exec(commandPrefix + command);
            process.waitFor(3, TimeUnit.SECONDS);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
            BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName("GBK")));
            int c = 3;
            while (process.isAlive() && c > 0 && process.getInputStream().available() <= 0 && process.getErrorStream().available() <= 0){
                c--;
                TimeUnit.MILLISECONDS.sleep(500);
            }
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            if(process.getInputStream().available() > 0) {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
            }
            if(stringBuilder.length() == 0 && process.getErrorStream().available() > 0){
                result.setSuccess(false);
                stringBuilder = new StringBuilder();
                while ((line = errorBufferedReader.readLine()) != null){
                    stringBuilder.append(line + "\n");
                }
            }
            result.setContent(stringBuilder.toString());
            log.info("执行命令结束["+command+"]");
        } catch (Exception e) {
            result.setSuccess(false);
            log.error("执行脚本失败", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    public static void execCommandWithOutResult(String commandPrefix,String command){
        Process process = null;
        try {
            log.info("开始执行命令["+command+"]");
            process = Runtime.getRuntime().exec(commandPrefix + command);
            process.waitFor(3, TimeUnit.SECONDS);
            log.info("执行命令结束["+command+"]");
        } catch (Exception e) {
            log.error("执行脚本失败", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static void execScript(String commandPrefix,String src){
        File file = new File(src);
        if(file.exists()) {
            Process process = null;
            try {
                log.info("开始执行脚本["+file.getAbsolutePath()+"]");
                process = Runtime.getRuntime().exec(commandPrefix + file.getAbsolutePath());
                process.waitFor(3, TimeUnit.SECONDS);
                log.info("执行脚本结束["+file.getAbsolutePath()+"]");
            } catch (Exception e) {
                log.error("执行脚本失败", e);
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }else{
            log.info("脚本文件不存在");
        }
    }
    public static class ShellResult{

        private ShellResult(){}

        private boolean isSuccess = true;

        private String content;

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
