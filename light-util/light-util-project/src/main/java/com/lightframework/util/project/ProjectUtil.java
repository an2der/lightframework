package com.lightframework.util.project;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * @author yg
 * @date 2023/9/1 14:51
 * @version 1.0
 */
public class ProjectUtil {

    private ProjectUtil(){}

    /**
     * 获取项目根目录
     * @return
     */
    public static String getProjectRootPath() {
        File baseFile = getBaseFile();
        try {
            if(baseFile != null && baseFile.isDirectory()){
                return URLDecoder.decode(baseFile.getAbsolutePath(),"UTF-8");
            }else {
                return URLDecoder.decode(baseFile.getParent(),"UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported Encoding 'UTF-8'",e);
        }
    }

    /**
     * 获取项目版本
     * @return
     */
    public static String getProjectVersion() {
        File baseFile = getBaseFile();
        if(baseFile != null && baseFile.isFile()){
            Manifest manifest;
            try {
                JarFile jarFile = new JarFile(baseFile);
                manifest = jarFile.getManifest();
            }catch (Exception e){
                throw new RuntimeException("Read file (MANIFEST.MF) error in " + baseFile.getAbsolutePath(),e);
            }
            if(manifest != null){
                return manifest.getMainAttributes().getValue("Implementation-Version");
            }else {
                throw new RuntimeException("Not found file (MANIFEST.MF) in " + baseFile.getAbsolutePath());
            }
        }else {
            File projectPath = new File(baseFile.getParentFile().getParent());
            File pomFile = new File(projectPath,"pom.xml");
            Model model;
            try {
                model = new MavenXpp3Reader().read(new FileInputStream(pomFile));
            } catch (Exception e) {
                throw new RuntimeException("Parse pom.xml error in " + pomFile.getAbsolutePath(),e);
            }
            String version = (model.getVersion() == null && model.getParent() != null)
                    || (model.getVersion() != null && model.getVersion().equals("${parent.version}"))
                    ?model.getParent().getVersion():model.getVersion();
            if(version == null){
                return null;
            }
            Pattern pattern = Pattern.compile("\\$\\{(((?!}).)+)\\}");
            Matcher matcher = pattern.matcher(version);
            if(matcher.find()) {
                Properties properties = getProjectPomAllProperties(projectPath);
                StringBuffer stringBuffer = new StringBuffer();
                do{
                    String key = matcher.group(1);
                    String val = properties.getProperty(key);
                    if(val != null) {
                        matcher.appendReplacement(stringBuffer, val);
                    }
                }while (matcher.find());
                matcher.appendTail(stringBuffer);
                return stringBuffer.toString();
            }else {
                return version;
            }
        }
    }

    private static Properties getProjectPomAllProperties(File projectPath){
        File pomFile = new File(projectPath,"pom.xml");
        Properties properties = new Properties();
        if(pomFile.exists()){
            try {
                Model model = new MavenXpp3Reader().read(new FileInputStream(pomFile));
                if(model != null){
                    if(model.getProperties() != null){
                        properties.putAll(model.getProperties());
                    }
                    if(model.getParent() != null){
                        Properties parentProperties = getProjectPomAllProperties(projectPath.getParentFile());
                        parentProperties.putAll(properties);
                        properties = parentProperties;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Parse pom.xml error in " + pomFile.getAbsolutePath(),e);
            }
        }
        return properties;
    }

    public static Manifest getProjectManifest() {
        File baseFile = getBaseFile();
        if(baseFile != null && baseFile.isFile()) {
            try {
                JarFile jarFile = new JarFile(baseFile);
                return jarFile.getManifest();
            } catch (IOException e) {
                throw new RuntimeException("Unable to read file!",e);
            }
        }
        return null;
    }

    public static File getProjectBaseFile(){
        return getBaseFile();
    }

    /**
     * 此类只能在本类中的其它方法直接调用使用，否则堆栈信息将获取错误
     * @return
     */
    private static File getBaseFile(){
        try {
            //获取到调用堆栈
            StackTraceElement [] stackTraceElements = new Throwable().getStackTrace();
            //通过调用堆栈反射出调用者类
            Class clazz = Class.forName(stackTraceElements[2].getClassName());
            //通过调用者类文件获取真实绝对路径
            return new File(clazz.getProtectionDomain().getCodeSource().getLocation().getFile());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class Not Found!",e);
        }
    }
}
