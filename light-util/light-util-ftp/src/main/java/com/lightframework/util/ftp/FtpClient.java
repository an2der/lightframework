package com.lightframework.util.ftp;

import com.lightframework.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** ftp
 * @author yg
 * @date 2022/4/26 10:57
 * @version 1.0
 */

@Slf4j
public class FtpClient extends BaseFtp {

    private static final FTPFileFilter systemFilter = new FTPFileFilter() {
        @Override
        public boolean accept(FTPFile ftpFile) {
            return !(".".equals(ftpFile.getName())||"..".equals(ftpFile.getName()));
        }
    };
    private FTPClient ftpClient= new FTPClient();

    public FtpClient(String host, String username, String password, String serverName) {
        super(host, Ftp.FTP_PORT, username, password, serverName);
    }

    public FtpClient(String host, int port, String username, String password, String serverName, String rootPath) {
        super(host, port, username, password, serverName, rootPath);
    }

    public FtpClient(String host, String username, String password, String serverName, String rootPath) {
        super(host, Ftp.FTP_PORT, username, password, serverName, rootPath);
    }

    public FtpClient(String host, String username, String password, String serverName, String rootPath, String encoding) {
        super(host, Ftp.FTP_PORT, username, password, serverName, rootPath, encoding);
    }

    public FtpClient(String host,int port, String username, String password, String serverName, String rootPath, String encoding) {
        super(host, port, username, password, serverName, rootPath, encoding);
    }

    public FtpClient(String host, int port, String username, String password, String serverName, String rootPath, String encoding, int connectTimeout) {
        super(host, port, username, password, serverName, rootPath, encoding, connectTimeout);
    }

    public FtpClient getNewInstance(){
        return new FtpClient(this.host,this.port,this.username,this.password,this.serverName,this.rootPath,this.encode,this.connectTimeout);
    }

    /**
     * 连接ftp服务器
     * @return
     */
    public boolean connect(){
        ftpClient = new FTPClient();
        try {
            ftpClient.setControlEncoding(encode);//设置编码格式，必须在连接之前
            ftpClient.setConnectTimeout(connectTimeout);//设置连接超时时间
            ftpClient.setDefaultTimeout(connectTimeout);//默认时间 防止卡死
            ftpClient.setListHiddenFiles(true);
            ftpClient.connect(host,port);//连接
            int reply = ftpClient.getReplyCode();   //获取状态码
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();        //结束连接
                log.error("FTP服务器连接异常![host="+host+",port="+port+",reply="+reply+"]" + ftpClient.getReplyString());
                throw new BusinessException("FTP服务器连接异常!");
            }
            if(!ftpClient.login(username,password)){//登录
                ftpClient.disconnect();
                log.error("FTP登录失败![host="+host+",port="+port+",username="+username+",password="+password+"]");
                throw new BusinessException("FTP登录失败!");
            }
            if(Charset.forName(encode) == StandardCharsets.UTF_8){
                ftpClient.sendCommand("OPTS UTF8","ON");
            }
            ftpClient.setFileTransferMode(FTPClient.STREAM_TRANSFER_MODE);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);//设置文件上传类型
            ftpClient.setControlKeepAliveTimeout(180);//设置上传时保持连接时间，单位s
            ftpClient.enterLocalPassiveMode();// 设置被动模式
            if(rootPath != null && !rootPath.isEmpty()){
                this.changeDir(rootPath);
            }
        } catch (IOException e) {
            log.error("FTP服务器连接异常![host="+host+",port="+port+"]",e);
            throw new BusinessException("FTP服务器连接异常!",e);
        }
        return true;
    }

    public boolean reConnect(){
        this.close();
        return this.connect();
    }

    /**
     * 关闭连接
     */
    public void close(){
        if(ftpClient != null && ftpClient.isConnected()){
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                log.error("FTP服务器关闭连接异常![host="+host+",port="+port+"]",e);
                throw new BusinessException("FTP服务器关闭连接异常!",e);
            }
        }
    }

    /**
     * 上传文件
     * @param filepath 服务器相对路径
     * @param inputStream 文件流
     * @return 保存结果
     */
    public boolean upload(String filepath,String filename, InputStream inputStream){
        try {
            return ftpClient.storeFile(filepath + separator + filename,inputStream);
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }


    /**
     * 文件下载
     * @param remotePath 服务器路径
     * @param fileName 服务器文件名称
     * @param localPath 保存到本地路径
     * @return 下载结果
     */
    public boolean download(String remotePath, String fileName, String localPath,String localName){
        String remoteFile = remotePath + separator + fileName;
        OutputStream outputStream = null;
        try {
            try {
                outputStream = new FileOutputStream(new File(localPath,localName));
                return ftpClient.retrieveFile(remoteFile,outputStream);
            }finally {
                if(outputStream != null)
                    outputStream.close();
            }
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }


    /**
     * 文件重命名
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @return 重命名结果
     */
    public boolean rename(String oldPath, String newPath){
        try {
            return ftpClient.rename(oldPath,newPath);
        } catch (IOException e) {
            log.error("文件重命名异常![host=" + host + ",oldPath=" + oldPath + ",newPath=" + newPath + "]",e);
            throw new BusinessException(e);
        }
    }

    /**
     * 删除FTP服务器文件
     * @param filepath 文件路径
     * @return
     */
    public boolean remove(String filepath){
        try {
            return ftpClient.deleteFile(filepath);
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    public boolean removeDir(String path){
        try {
            FTPFile[] files = ftpClient.listFiles(path,systemFilter);
            for(FTPFile file : files){
                String filePath = path + separator + file.getName();
                boolean b;
                if(file.isDirectory()){
                    b = removeDir(filePath);
                }else{
                    b = ftpClient.deleteFile(rootPath + separator + filePath);
                }
                if(!b){
                    return b;
                }
            }
            return ftpClient.removeDirectory(path);
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    /**
     * 获取文件列表
     * @return
     */
    public List<FtpNode> list(String path){
        try {
            FTPFile[] files = ftpClient.listFiles(path,systemFilter);
            String parent = path.lastIndexOf(separator) == path.length() - 1?path.substring(0,path.length() - 1):path;
            return Arrays.stream(files).sorted(Comparator.comparing(FTPFile::getName,String.CASE_INSENSITIVE_ORDER)).sorted(Comparator.comparing(FTPFile::isDirectory, Comparator.reverseOrder())).map(f ->
                    new FtpNode() {{
                        setName(f.getName().trim());
                        setParentPath(parent);
                        setPath(getParentPath() + "/" + getName());
                        setCreateTime(new Date(f.getTimestamp().getTimeInMillis() +
                                f.getTimestamp().getTimeZone().getOffset(0)));
                        setModifyTime(getCreateTime());
                        setSize(f.getSize());
                        setDirectory(f.isDirectory());
                    }}
            ).collect(Collectors.toList());
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    public void mkdir(String path){
        try {
            int lastSepIndex = path.lastIndexOf(separator);
            if(changeDir(path.substring(0,lastSepIndex))){
                ftpClient.makeDirectory(path.substring(lastSepIndex+1));
            }else {
                for (String d : path.split(separator)) {
                    if (d != null && !d.trim().isEmpty() && !ftpClient.changeWorkingDirectory(d)) {
                        ftpClient.makeDirectory(d);
                        ftpClient.changeWorkingDirectory(d);
                    }
                }
            }
        }catch (IOException e){
            throw new BusinessException(e);
        }
    }


    /**
     * 验证FTP服务器文件是否存在
     * @param path 服务器路径
     * @param fileName 服务器文件名称
     * @param isDir 是否是文件夹
     * @return 是否存在
     */
    public boolean validateRemoteFileExist(String path, String fileName,boolean isDir){
        try {
            FTPFile[] arr = ftpClient.listFiles(path, new FTPFileFilter() {
                @Override
                public boolean accept(FTPFile ftpFile) {
                    return ftpFile.getName().equals(fileName) && (isDir?ftpFile.isDirectory():ftpFile.isFile());
                }
            });
            return arr != null && arr.length > 0;
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    /**
     * 改变工作目录
     * @param path 服务器路径
     * @return 改变路径结果
     */
    public boolean changeDir(String path){
        try {
            return ftpClient.changeWorkingDirectory(path);
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    /**
     * 连接状态
     * @return
     */
    public boolean isConnected(){
        return ftpClient.isConnected();
    }

    /**
     * 当前工作目录
     * @return
     */
    public String currentDir(){
        try {
            String[] rt = ftpClient.doCommandAsStrings("pwd","");
            Pattern p=Pattern.compile("\".+\"");
            Matcher m=p.matcher(rt[0]);
            if(m.find()){
                return m.group(0).replace("\"","");
            }
            return null;
        } catch (IOException e) {
            throw new BusinessException(e);
        }
    }

    public boolean getStatus(){
        try {
            return ftpClient.sendNoOp();
        } catch (Exception e) {
            return false;
        }
    }

}


