package com.lightframework.comm.ftp;

import java.util.List;

/** 所有方法路径参数传全路径
 * @author yg
 * @date 2022/10/20 14:58
 * @version 1.0
 */
public abstract class BaseFtp implements Ftp{

    protected String encode = "UTF-8";

    protected int connectTimeout = 6000;

    protected String host;

    protected int port;

    protected String username;

    protected String password;

    protected String serverName;

    protected String rootPath;

    private BaseFtp(){}


    public BaseFtp(String host, int port, String username, String password, String serverName) {
        this(host,port,username,password,serverName,null);
    }

    public BaseFtp(String host, int port, String username, String password, String serverName, String rootPath) {
        this(host, port, username, password, serverName,rootPath,null);
    }

    public BaseFtp(String host, int port, String username, String password, String serverName, String rootPath,String encode) {
        this(host, port, username, password, serverName, rootPath, encode,0);
    }

    public BaseFtp(String host, int port, String username, String password, String serverName, String rootPath, String encode, int connectTimeout) {
        if(encode != null) {
            this.encode = encode;
        }
        this.host = host;
        this.port = port;
        this.serverName = serverName;
        this.username = username;
        this.password = password;
        if(connectTimeout > 0) {
            this.connectTimeout = connectTimeout;
        }
        this.rootPath = rootPath;
    }

    public String getHost() {
        return host;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRootPath() {
        return rootPath;
    }

    @Override
    public abstract BaseFtp getNewInstance();


    @Override
    public boolean uploadForFolder(String sourcePath, String destinationPath) {
        return false;
    }

    @Override
    public boolean downloadForFolder(String sourcePath, String destinationPath,boolean conflictOverwrite, List<String> includes, List<String> excludes) {
        return false;
    }

}
