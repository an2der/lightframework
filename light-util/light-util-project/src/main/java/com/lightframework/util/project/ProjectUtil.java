package com.lightframework.util.project;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
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
        String enc = "UTF-8";
        try {
            URL url = ProjectUtil.class.getClassLoader().getResource("");
            if (url != null) {
                return URLDecoder.decode((new File(url.getFile()).getAbsolutePath()),enc);
            }
            String userDir = System.getProperty("user.dir");
            if (userDir != null && !userDir.isEmpty()) {
                return URLDecoder.decode((new File(userDir).getAbsolutePath()),enc);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported Encoding "+enc,e);
        }
        File baseFile = getClassBaseFile(getClazz());
        if (baseFile != null && baseFile.isDirectory()) {
            return baseFile.getAbsolutePath();
        } else {
            return baseFile.getParent();
        }
    }

    /**
     * 获取项目根路径
     * @param startMainClass 项目主启动类
     * @return
     */
    public static String getProjectRootPath(Class startMainClass){
        File baseFile = getClassBaseFile(startMainClass);
        if (baseFile != null && baseFile.isDirectory()) {
            return baseFile.getAbsolutePath();
        } else {
            return baseFile.getParent();
        }
    }


    /**
     * 获取项目版本号，在MAVEN多模块的项目中是获取的当前模块的
     * 如果是在非启动模块调用就使用传启动类参数的方法
     * @return
     */
    public static String getThisProjectVersion() {
        return getProjectVersion(getClazz());
    }

    /**
     * 获取项目版本号
     * @param startMainClass 项目主启动类
     * @return
     */
    public static String getProjectVersion(Class startMainClass) {
        File baseFile = getClassBaseFile(startMainClass);
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
            if(!pomFile.exists()){
                throw new RuntimeException(pomFile.getAbsolutePath() + " file does not exist!");
            }
            FileInputStream fileInputStream = null;
            Model model;
            try {
                fileInputStream = new FileInputStream(pomFile);
                model = new MavenXpp3Reader().read(fileInputStream);
            } catch (Exception e) {
                throw new RuntimeException("Parse pom.xml error in " + pomFile.getAbsolutePath(),e);
            }finally {
                if(fileInputStream != null){
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                }
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
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(pomFile);
                Model model = new MavenXpp3Reader().read(fileInputStream);
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
            }finally {
                if(fileInputStream != null){
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return properties;
    }

    public static Manifest getThisProjectManifest() {
        return getProjectManifest(getClazz());
    }

    public static Manifest getProjectManifest(Class startMainClass) {
        File baseFile = getClassBaseFile(startMainClass);
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

    /**
     * 获取调用这个方法的类的文件所在位置，在MAVEN多模块的项目中是获取的当前模块的
     * @return
     */
    public static File getThisClassBaseFile(){
        return getClassBaseFile(getClazz());
    }

    private static File getClassBaseFile(Class clazz){
        try {
            //通过调用者类文件获取真实绝对路径
            return new File(URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getFile(),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported Encoding UTF-8",e);
        }
    }

    /**
     * 此方法只能在本类中的其它方法直接调用使用，否则堆栈信息将获取错误
     * @return
     */
    private static Class getClazz(){
        try {
            //获取到调用堆栈
            StackTraceElement [] stackTraceElements = new Throwable().getStackTrace();
            //通过调用堆栈反射出调用者类
            return Class.forName(stackTraceElements[2].getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class Not Found!",e);
        }
    }

}
