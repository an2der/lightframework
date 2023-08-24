package com.lightframework.util.ftp;

import java.io.InputStream;
import java.util.List;

/** 所有方法路径参数传全路径
 * @author yg
 * @date 2022/10/20 14:57
 * @version 1.0
 */
public interface Ftp {

    String separator = "/";

    String FTP = "FTP";

    int FTP_PORT = 21;

    String SFTP = "SFTP";

    int SFTP_PORT = 22;


    Ftp getNewInstance();

    /**
     * 连接ftp服务器
     * @return
     */
    boolean connect();

    default boolean reConnect(){
        close();
        return connect();
    }

    /**
     * 关闭连接
     */
    void close();

    /**
     * 上传文件
     * @param filepath 服务器相对路径
     * @param inputStream 文件流
     * @return 保存结果
     */
    boolean upload(String filepath,String filename, InputStream inputStream);

    /**
     * 上传文件夹
     * @param sourcePath 本地文件夹路径
     * @param destinationPath 服务器相对路径
     */
    boolean uploadForFolder(String sourcePath, String destinationPath);

    /**
     * 文件下载
     * @param remotePath 服务器路径
     * @param fileName 服务器文件名称
     * @param localPath 保存到本地路径
     * @param localName 保存到本地名称
     * @return 下载结果
     */
    boolean download(String remotePath, String fileName, String localPath,String localName);

    /**
     * 下载文件夹
     * @param sourcePath
     * @param destinationPath
     */
    boolean downloadForFolder(String sourcePath, String destinationPath,boolean conflictOverwrite,List<String> includes,List<String> excludes);

    /**
     * 文件重命名
     * @param oldPath 旧文件路径
     * @param newPath 新文件路径
     * @return 重命名结果
     */
    boolean rename(String oldPath, String newPath);

    /**
     * 删除FTP服务器文件
     * @param filepath 文件路径
     * @return
     */
    boolean remove(String filepath);

    boolean removeDir(String path);

    /**
     * 获取文件列表
     * @return
     */
    List<FtpNode> list(String path);

    void mkdir(String path);


    /**
     * 验证FTP服务器文件是否存在
     * @param path 服务器路径
     * @param fileName 服务器文件名称
     * @param isDir 是否是文件夹
     * @return 是否存在
     */
    boolean validateRemoteFileExist(String path, String fileName,boolean isDir);

    /**
     * 改变工作目录
     * @param path 服务器路径
     * @return 改变路径结果
     */
    boolean changeDir(String path);

    /**
     * 连接状态
     * @return
     */
    boolean isConnected();

    /**
     * 当前工作目录
     * @return
     */
    String currentDir();

    boolean getStatus();
}
