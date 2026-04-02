package com.lightframework.util.command.application;

import com.lightframework.common.LightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/***
 * @author yg
 * @date 2022/5/23 15:50
 * @version 1.0
 */
public class ApplicationStarter {

    private ApplicationStarter(){}

    private static Logger log = LoggerFactory.getLogger(ApplicationStarter.class);

    public static ApplicationResult start(String path){
        return start(new File(path));
    }

    public static ApplicationResult start(File file){
        ApplicationResult result = new ApplicationResult();
        try {
            if(!file.exists()){
                result.setSuccess(false);
                result.setContent("应用程序路径不存在");
            }
            if(file.isDirectory()){
                result.setSuccess(false);
                result.setContent("应用程序路径不正确");
            }
            new ProcessBuilder(file.getAbsolutePath())
                    .directory(file.getParentFile()).start();
        } catch (Exception e) {
            result.setSuccess(false);
            throw new LightException(e);
        }
        return result;
    }

}
