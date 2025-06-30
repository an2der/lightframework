package com.lightframework.util.shell;

import cn.hutool.core.thread.ThreadUtil;
import com.lightframework.common.LightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/***
 * @author yg
 * @date 2022/5/23 15:50
 * @version 1.0
 */
public class CommandExecutor {

    private CommandExecutor(){}

    private static Logger log = LoggerFactory.getLogger(CommandExecutor.class);

    public static CommandResult exec(CommandLine command){
        return exec(command,Charset.forName("GBK"),0);
    }

    public static CommandResult exec(CommandLine command, Charset charset){
        return exec(command,charset,0);
    }

    public static CommandResult exec(CommandLine command, long waitTimeSeconds){
        return exec(command,Charset.forName("GBK"),waitTimeSeconds);
    }

    public static CommandResult exec(CommandLine command, Charset charset, long waitTimeSeconds){
        Process process = null;
        CommandResult result = new CommandResult();
        try {
            process = Runtime.getRuntime().exec(command.getCommand());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
            StringBuilder stringBuilder = new StringBuilder();
            ThreadUtil.execute(()->{
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                }catch (Exception e){
                    log.error("执行命令是读取标准输入流发生异常",e);
                }
            });
            BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));
            StringBuilder errorStringBuilder = new StringBuilder();
            ThreadUtil.execute(()->{
                String line;
                try {
                    while ((line = errorBufferedReader.readLine()) != null){
                        result.setSuccess(false);
                        errorStringBuilder.append(line + "\n");
                    }
                }catch (Exception e){
                    log.error("执行命令是读取标准异常流发生异常",e);
                }
            });

            try (OutputStream outputStream = process.getOutputStream()) {
                outputStream.write("\r\n".getBytes());
                outputStream.flush();
            }
            if(waitTimeSeconds > 0) {
                process.waitFor(waitTimeSeconds, TimeUnit.SECONDS);
                if(process.isAlive()){
                    result.setSuccess(false);
                    result.setContent("执行命令超过等待时间(" + waitTimeSeconds + "秒)，强制终止命令执行！" + "\n");
                    result.setExitVal(-1);
                    process.destroy();
                }
            }else {
                process.waitFor();
            }
            if(result.getExitVal() != -1) {
                result.setExitVal(process.exitValue());
            }
            if(result.getContent() == null) {
                result.setContent(result.isSuccess() ? stringBuilder.toString() : errorStringBuilder.toString());
            }
        } catch (Exception e) {
            result.setSuccess(false);
            throw new LightException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
}
