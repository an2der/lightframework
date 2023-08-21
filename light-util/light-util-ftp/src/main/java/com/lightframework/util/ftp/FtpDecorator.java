package com.lightframework.util.ftp;

import com.lightframework.common.BusinessException;
import com.lightframework.util.ftp.tool.ToolKit;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;

/** ftp装饰器
 * @author yg
 * @date 2022/10/20 16:40
 * @version 1.0
 */
@Slf4j
public class FtpDecorator implements Ftp{

    private BaseFtp baseFtp;

    private FtpDecorator(){}

    public FtpDecorator(BaseFtp baseFtp) {
        this.baseFtp = baseFtp;
    }

    public BaseFtp getBaseFtp() {
        return baseFtp;
    }

    @Override
    public FtpDecorator getNewInstance() {
        return new FtpDecorator(baseFtp.getNewInstance());
    }

    @Override
    public boolean connect() {
        if(baseFtp.connect()){
            if(baseFtp.rootPath != null && !baseFtp.rootPath.isEmpty()){
                this.changeDir(baseFtp.rootPath);
            }
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void close() {
        baseFtp.close();
    }

    @Override
    public boolean upload(String filepath, String filename, InputStream inputStream) {
        try {
            try {
                mkdir(filepath);
                return baseFtp.upload(filepath,filename,inputStream);
            }finally {
                if(inputStream != null)
                    inputStream.close();
            }
        }catch (IOException e){
            log.error("上传文件发生异常![host=" + baseFtp.host + ",filepath=" + filepath + "]", e);
            throw new BusinessException("上传文件发生异常!");
        }catch (BusinessException e) {
            if(needReconnection(e)){
                return upload(filepath,filename,inputStream);
            }else {
                log.error("上传文件发生异常![host=" + baseFtp.host + ",filepath=" + filepath + "]", e);
                throw e;
            }
        }
    }

    public boolean upload(String filepath, String filename, String localPath) {
        File file = new File(localPath);
        if(!file.exists()){
            log.error("文件不存在![host="+baseFtp.host+",filename="+filepath+"]");
            throw new BusinessException("文件不存在!");
        }
        try {
            return upload(filepath,filename,new FileInputStream(file));
        } catch (FileNotFoundException e) {
            log.error("拒绝访问异常![host="+baseFtp.host+",filepath="+filepath+"]",e);
            throw new BusinessException("拒绝访问");
        }
    }

    @Override
    public boolean uploadForFolder(String sourcePath, String destinationPath) {
        File file = new File(sourcePath);
        if(!file.exists() || !file.isDirectory()){
            return false;
        }
        mkdir(destinationPath);
        File [] files = file.listFiles();
        try {
            for (File f : files) {
                boolean b;
                if(f.isFile()){
                    b = this.upload(destinationPath,f.getName(),new FileInputStream(f));
                }else {
                    b = uploadForFolder(sourcePath+File.separator+f.getName(),destinationPath + baseFtp.separator + f.getName());
                }
                if(!b){
                    return b;
                }
            }
            return true;
        } catch (IOException e) {
            log.error("文件夹上传异常![host="+baseFtp.host+",sourcePath="+sourcePath+",destinationPath="+destinationPath+"]",e);
            throw new BusinessException("文件夹上传异常");
        }
    }

    @Override
    public boolean download(String remotePath, String fileName, String localPath, String localName) {
        return downloadToLinux(remotePath, fileName, localPath, localName);
    }

    @Override
    public boolean downloadForFolder(String sourcePath, String destinationPath) {
        return downloadForFolderToLinux(sourcePath, destinationPath);
    }

    public boolean download(String remotePath, String fileName, String localPath,String localName, boolean toWindows) {
        if(!validateRemoteFileExist(remotePath,fileName,false)){
            log.error("文件不存在![host="+baseFtp.host+",remotePath="+remotePath+",filename="+fileName+"]");
            throw new BusinessException("文件不存在!");
        }
        try {
            String clearLocalPath = localPath;
            String clearLocalName = localName;
            if(toWindows){ //处理windows路径特殊符号与文件名大小写冲突
                clearLocalPath = ToolKit.clearWindowsFilePathSpecialCharacter(localPath);
                clearLocalName = ToolKit.clearWindowsFileNameSpecialCharacter(localName);
                ToolKit.clearDuplicateNameFiles(new File(clearLocalPath,clearLocalName), false);
            }
            File file = new File(clearLocalPath);
            if(!file.exists()){
                file.mkdirs();
            }
            return baseFtp.download(remotePath, fileName, clearLocalPath,clearLocalName);
        } catch (IOException e) {
            log.error("文件下载异常![host=" + baseFtp.host + ",remotePath=" + remotePath + ",filename=" + fileName + "]",e);
            throw new BusinessException("文件下载异常");
        }catch (BusinessException e) {
            if(needReconnection(e)){
                return download(remotePath,fileName,localPath,localName,toWindows);
            }else {
                log.error("文件下载异常![host=" + baseFtp.host + ",remotePath=" + remotePath + ",filename=" + fileName + "]",e);
                throw e;
            }
        }
    }

    public boolean downloadToWindows(String remotePath, String fileName, String localPath,String localName) {
        return this.download(remotePath,fileName,localPath,localName,true);
    }

    public boolean downloadToLinux(String remotePath, String fileName, String localPath,String localName) {
        return this.download(remotePath,fileName,localPath,localName,false);
    }

    public boolean downloadForFolder(String sourcePath, String destinationPath, boolean toWindows) {
        try {
            if (!this.changeDir(sourcePath)) {
                throw new BusinessException("目录不存在!");
            }
            List<FtpNode> files = list(sourcePath);
            if(files != null && files.size() > 0) {
                String clearDestinationPath = toWindows? ToolKit.clearWindowsFilePathSpecialCharacter(destinationPath):destinationPath;
                File file = new File(clearDestinationPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (FtpNode f : files) {
                    String clearDestinationName = f.getName();
                    if(toWindows) {
                        clearDestinationName = ToolKit.clearWindowsFileNameSpecialCharacter(f.getName());
                        ToolKit.clearDuplicateNameFiles(new File(clearDestinationPath,clearDestinationName), f.isDirectory());
                    }
                    boolean b;
                    if (f.isDirectory()) {
                        b = downloadForFolder(sourcePath + baseFtp.separator + f.getName(), Paths.get(clearDestinationPath,clearDestinationName).toString(),toWindows);
                    } else {
                        b = baseFtp.download(sourcePath,f.getName(),clearDestinationPath,clearDestinationName);
                    }
                    if (!b) {
                        return b;
                    }
                }
            }
            return true;
        } catch (IOException e) {
            log.error("文件夹下载异常![host=" + baseFtp.host + ",sourcePath=" + sourcePath + ",destinationPath=" + destinationPath + "]",e);
            throw new BusinessException("文件夹下载异常");
        }catch (BusinessException e) {
            if(needReconnection(e)){
                return downloadForFolder(sourcePath, destinationPath,toWindows);
            }else {
                log.error("文件夹下载异常![host=" + baseFtp.host + ",sourcePath=" + sourcePath + ",destinationPath=" + destinationPath + "]",e);
                throw e;
            }
        }
    }

    public boolean downloadForFolderToWindows(String sourcePath, String destinationPath) {
        return this.downloadForFolder(sourcePath,destinationPath,true);
    }

    public boolean downloadForFolderToLinux(String sourcePath, String destinationPath) {
        return this.downloadForFolder(sourcePath,destinationPath,false);
    }

    @Override
    public boolean rename(String oldPath, String newPath) {
        try {
            return baseFtp.rename(oldPath,newPath);
        } catch (BusinessException e) {
            if(needReconnection(e)){
                return rename(oldPath,newPath);
            }else {
                throw e;
            }
        }
    }

    @Override
    public boolean remove(String filepath) {
        if(filepath == null || filepath.trim().length() == 0){
            throw new BusinessException("删除文件路径不能为空");
        }
        try {
            int lastSepIndex = filepath.lastIndexOf(separator);
            if(validateRemoteFileExist(filepath.substring(0,lastSepIndex),filepath.substring(lastSepIndex+1),false)){
                return baseFtp.remove(filepath);
            }
            return true;
        } catch (BusinessException e) {
            if(needReconnection(e)){
                return remove(filepath);
            }else {
                log.error("文件删除异常![host=" + baseFtp.host + ",filepath=" + filepath + "]",e);
                throw e;
            }
        }
    }

    @Override
    public boolean removeDir(String path) {
        if(path == null || path.isEmpty()){
            throw new BusinessException("删除文件夹路径不能为空");
        }
        try {
            int lastSepIndex = path.lastIndexOf(separator);
            if(validateRemoteFileExist(path.substring(0,lastSepIndex),path.substring(lastSepIndex+1),true)){
                return baseFtp.removeDir(path);
            }
            return true;
        } catch (BusinessException e) {
            if(needReconnection(e)){
                return removeDir(path);
            }else {
                log.error("文件夹删除异常![host=" + baseFtp.host + ",path=" + path + "]",e);
                throw e;
            }
        }
    }

    @Override
    public List<FtpNode> list(String path) {
        try {
            if (!this.changeDir(path)) {
                throw new BusinessException("目录不存在或无法访问!");
            }
            return baseFtp.list(path);
        } catch (BusinessException e) {
            if(needReconnection(e)){
                return list(path);
            }else {
                log.error("获取文件列表异常![host=" + baseFtp.host + "]",e);
                throw e;
            }
        }
    }

    @Override
    public void mkdir(String path) {
        try {
            if(path != null && path.trim().length() > 0) {
                baseFtp.mkdir(path);
            }
        } catch (BusinessException e) {
            if(needReconnection(e)){
                mkdir(path);
            }else {
                log.error("创建文件夹异常![host=" + baseFtp.host + ",path=" + path + "]",e);
                throw e;
            }
        }
    }

    @Override
    public boolean validateRemoteFileExist(String path, String fileName,boolean isDir) {
        try {
            return baseFtp.validateRemoteFileExist(path,fileName,isDir);
        } catch (BusinessException e) {
            if(needReconnection(e)){
                return validateRemoteFileExist(path,fileName,isDir);
            }else {
                log.error("获取FTP服务器文件列表异常![host=" + baseFtp.host + ",path=" + path + "]",e);
                throw e;
            }
        }
    }

    @Override
    public boolean changeDir(String path) {
        try {
            if(path != null && path.trim().length() > 0) {
                return baseFtp.changeDir(path);
            }
            return false;
        } catch (BusinessException e) {
            if(needReconnection(e)){
                return changeDir(path);
            }else {
                log.error("改变FTP工作路径失败![host=" + baseFtp.host + ",path=" + path + "]",e);
                throw e;
            }
        }
    }

    @Override
    public boolean isConnected() {
        return baseFtp.isConnected();
    }

    @Override
    public String currentDir() {
        try {
            return baseFtp.currentDir();
        } catch (BusinessException e) {
            if(needReconnection(e)){
                return currentDir();
            }else {
                log.error("获取当前文件夹异常![host=" + baseFtp.host + "]",e);
                throw e;
            }
        }
    }

    @Override
    public boolean getStatus() {
        return baseFtp.getStatus();
    }

    private boolean needReconnection(BusinessException e){
        return e.getCause() != null && !getStatus() && reConnect();
    }

}
