package com.lightframework.comm.svn;

import cn.hutool.core.io.unit.DataSizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SvnClient {

    private Logger logger = LoggerFactory.getLogger(SvnClient.class);

    // 更新状态 true:没有程序在执行任务，反之则反
    private volatile boolean DoUpdateStatus = false;

    // 声明SVN客户端管理类
    private SVNClientManager clientManager;

    // 声明SVN服务端仓储对象
    private SVNRepository svnRepository;

    private String svnUrl;

    private String localPath;

    private SvnClient(){}

    public SvnClient(String svnUrl, String localPath, String userName, String password){
        this.svnUrl = svnUrl;
        this.localPath = localPath;
        initClientAuthSvn(svnUrl,userName,password);
    }


    /**
     * 通过不同的协议初始化版本库
     */
    private void setupLibrary() {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }

    /**
     * 验证登录svn返回SVN客户端管理类
     */
    private void initClientAuthSvn(String svnUrl, String userName, String password) {
        // 初始化版本库
        setupLibrary();
        // 创建库连接
        try {
            svnRepository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnUrl));
        } catch (SVNException e) {
            throw new RuntimeException(e);
        }
        // 身份验证
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password.toCharArray());
        // 创建身份验证管理器
        svnRepository.setAuthenticationManager(authManager);
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        SVNClientManager svnClientManager = SVNClientManager.newInstance(options, authManager);
        clientManager = svnClientManager;
    }

    /**
     * 验证登录svn返回SVN服务端仓储对象
     */
    private void initSVNRepositoryAuthSvn(String svnUrl, String userName, String password) throws SVNException {
        // 初始化版本库
        setupLibrary();
        SVNURL repositoryURL = SVNURL.parseURIEncoded(svnUrl);
        svnRepository = SVNRepositoryFactory.create(repositoryURL);
        ISVNAuthenticationManager isvnAuthenticationManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password.toCharArray());
        svnRepository.setAuthenticationManager(isvnAuthenticationManager);
    }

    private void initClientManager(String userName, String password) {
        setupLibrary();
        ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
        // 实例化客户端管理类
        clientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, userName, password);
    }

    public SVNClientManager getClientManager() {
        return clientManager;
    }

    public SVNRepository getSvnRepository() {
        return svnRepository;
    }

    public boolean isBusy() {
        return DoUpdateStatus;
    }

    public boolean isWorkingCopy() {
        boolean result = false;
        File filePath = new File(localPath);
        if (filePath.exists()) {
            try {
                File fileRes = SVNWCUtil.getWorkingCopyRoot(filePath, false);
                if (fileRes != null) {
                    result = true;
                }
            } catch (SVNException e) {
                logger.error("检查文件是否工作区失败：", e);
            }
        }
        return result;
    }


    /**
     * svn检出
     *
     * @return 检出结果
     * @throws SVNException svn处理异常
     */
    public void checkout(ISVNEventHandler handler,boolean allowUnversionedObstructions) throws SVNException {
        DoUpdateStatus = true;
        try {
            // 相关变量赋值
            SVNURL repositoryURL = SVNURL.parseURIEncoded(svnUrl);
            // 要把版本库的内容check out到的目录
            File wcDir = new File(localPath);
            // 通过客户端管理类获得updateClient类的实例。
            SVNUpdateClient updateClient = clientManager.getUpdateClient();
            updateClient.setIgnoreExternals(false);
            // 执行check out 操作，返回工作副本的版本号。
            updateClient.setEventHandler(handler);
            updateClient.doCheckout(repositoryURL, wcDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, allowUnversionedObstructions);
        } finally {
            clientManager.getUpdateClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }

    /**
     * 清理目录下的svn信息
     *
     * @param removeUnversionedItems 是否删除未被版本控制的文件
     * @return 执行结果
     */
    public boolean doCleanup(boolean removeUnversionedItems, ISVNEventHandler handler) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        DoUpdateStatus = true;
        try {
            File wcDir = new File(localPath);
            if (wcDir.exists()) {
                clientManager.getWCClient().setEventHandler(handler);
                clientManager.getWCClient().doCleanup(wcDir, false, true, true, removeUnversionedItems, false, true);
                return true;
            }
            return false;
        }finally {
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }

    /**
     * svn revert
     *
     * @param filePath      文件夹路径
     * @param deleteAddFile 是否删除新增的文件
     * @return 执行结果
     */
    public void doRevert(List<String> filePath, boolean deleteAddFile,String []ignores, ISVNEventHandler handler) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            if (filePath != null && filePath.size() > 0) {
                DoUpdateStatus = true;
                String rootPath = new File(localPath,File.separator).getPath();
                clientManager.getWCClient().setEventHandler(handler);
                List<File> allList = new ArrayList<>();
                for (String path : filePath) {
                    File file = new File(localPath,path);
                    clientManager.getStatusClient().doStatus(file, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, true, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) throws SVNException {
                            if(status.getNodeStatus() == SVNStatusType.STATUS_CONFLICTED ||(ignores == null || Arrays.stream(ignores).noneMatch(s->status.getFile().getPath().length() > rootPath.length() && status.getFile().getPath().substring(rootPath.length()).toLowerCase().indexOf(s.toLowerCase()) == 0))) {
                                SVNStatusType mynodeStatus = status.getNodeStatus();
                                if (deleteAddFile && mynodeStatus.equals(SVNStatusType.STATUS_UNVERSIONED)) {
                                    delete(status.getFile(),rootPath,ignores,handler);
                                }else if( mynodeStatus.equals(SVNStatusType.STATUS_ADDED)){
                                    clientManager.getWCClient().doDelete(status.getFile(), true, true, false);
                                }else {
                                    allList.add(status.getFile());
                                }
                            }
                        }
                    }, null);
                }
                if (allList.size() > 0) {
                    clientManager.getWCClient().doRevert(allList.stream().toArray(File[]::new), SVNDepth.INFINITY, null);
                }
            }
        } finally {
            clientManager.getStatusClient().getOperationsFactory().dispose();
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }

    /**
     * 设置ignore属性值
     * @param ignores
     * @throws SVNException
     */
    public void setIgnore(String [] ignores) throws SVNException {
        try {
            File file = new File(localPath);
            SVNPropertyValue value = null;
            if(ignores != null && ignores.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (String pattern : ignores) {
                    if(!"".equals(pattern.trim())) {
                        sb.append(pattern).append("\n");
                    }
                }
                if(sb.length() > 0) {
                    value = SVNPropertyValue.create(sb.toString());
                }
            }
            clientManager.getWCClient().doSetProperty(file,SVNProperty.IGNORE,value,false,SVNDepth.EMPTY,null,null);
            clientManager.getCommitClient().doCommit(new File[]{file},true,"",null,null,false,true,SVNDepth.EMPTY);
        }finally {
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.dispose();
        }
    }

    /**
     * 获取ignore属性值
     * @return
     * @throws SVNException
     */
    public String [] getIgnore() throws SVNException {
        try {
            SVNPropertyData data = clientManager.getWCClient().doGetProperty(new File(localPath),SVNProperty.IGNORE,SVNRevision.HEAD,SVNRevision.HEAD);
            if(data == null){
                return null;
            }
            return Arrays.stream(data.getValue().getString().split("\n")).map(s->s.trim()).toArray(String[]::new);
        } finally {
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.dispose();
        }
    }

    public void deleteFile(List<File> list,String rootPath,String []ignores, ISVNEventHandler handler) throws SVNException {
        for (File file : list) {
            if (file.exists())
                delete(file,rootPath,ignores,handler);
        }
    }

    public boolean delete(File file,String rootPath,String []ignores, ISVNEventHandler handler) throws SVNException {
        if(ignores == null || Arrays.stream(ignores).noneMatch(s->file.getPath().length() > rootPath.length() && file.getPath().substring(rootPath.length()).toLowerCase().indexOf(s.toLowerCase()) == 0)) {
            boolean b = true;
            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (childFiles != null && childFiles.length > 0) {
                    for (int i = 0; i < childFiles.length; i++) {
                        if(!delete(childFiles[i],rootPath, ignores,handler)){
                            b = false;
                        }
                    }
                }
            }
            if(b) {
                SVNNodeKind kind = file.isDirectory()?SVNNodeKind.DIR:SVNNodeKind.FILE;
                boolean r = file.delete();
                if(handler != null && r){
                    SVNEvent svnEvent = new SVNEvent(file,kind,null,-1,null,null,null,null,SVNEventAction.DELETE,null,null,null,null,null,null);
                    handler.handleEvent(svnEvent,-1);
                }
                return r;
            }
        }
        return false;
    }


    /**
     * 更新svn
     *
     * @param files 文件相对路径
     * @return int(- 1更新失败 ， 1成功 ， 0有程序在占用更新)
     */
    public void doUpdate(List<String> files,SVNRevision revision, SVNDepth depth , ISVNEventHandler handler) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        DoUpdateStatus = true;
        try {
            // 获得updateClient的实例
            SVNUpdateClient updateClient = clientManager.getUpdateClient();
            updateClient.setEventHandler(handler);
            updateClient.setIgnoreExternals(false);
            // 执行更新操作
            updateClient.doUpdate(files.stream().map(s->new File(localPath,s)).toArray(File[]::new), revision, depth, true, false);
        }finally {
            clientManager.getUpdateClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }

    /**
     * 更新至最新
     * @param files
     * @param handler
     * @throws SVNException
     */
    public void doUpdateToLatest(List<String> files,ISVNEventHandler handler) throws SVNException {
        this.doUpdate(files,SVNRevision.HEAD,SVNDepth.INFINITY,handler);
    }


    /**
     * 将文件导入并提交到svn 同路径文件要是已经存在将会报错
     *
     * @param svnURL      远程根路径
     * @param dirPath     文件所在文件夹的路径
     * @param isRecursive 是否递归
     * @return boolean 返回结果
     * @throws SVNException svn异常
     */
    public boolean doImport(String svnURL, String dirPath, boolean isRecursive, String commitMessage) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            DoUpdateStatus = true;
            SVNURL repositoryURL = SVNURL.parseURIEncoded(svnURL);
            // 要把此目录中的内容导入到版本库
            File impDir = new File(dirPath);
            // 执行导入操作
            clientManager.getCommitClient().doImport(impDir, repositoryURL,
                    commitMessage, null,
                    true, true,
                    SVNDepth.fromRecurse(isRecursive));
        }finally {
            clientManager.getCommitClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
        return true;
    }

    /**
     * 预提交,并返回提交列表
     *
     * @param filePath 待提交文件目录相对路径
     * @return 提交结果
     * @throws SVNException svn异常
     */
    public List<SvnNode> preCommitAll(List<String> filePath) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            List<SvnNode> allList = new ArrayList<>();
            if (filePath != null && filePath.size() > 0) {
                DoUpdateStatus = true;
                for (String path : filePath) {
                    File file = new File(localPath,path);
                    clientManager.getStatusClient().doStatus(file, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, true, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) throws SVNException {
                            SVNStatusType mynodeStatus = status.getNodeStatus();
                            File file = status.getFile();
                            SvnNode svnNode = new SvnNode();
                            svnNode.setPath(file.getAbsolutePath());
                            svnNode.setName(file.getName());
                            svnNode.setParentPath(file.getParent());
                            svnNode.setSize(file.length());
                            svnNode.setSizeStr(DataSizeUtil.format(svnNode.getSize()));
                            svnNode.setDate(new Date(file.lastModified()));
                            svnNode.setDir(mynodeStatus == SVNStatusType.STATUS_UNVERSIONED?file.isDirectory():status.getKind() != null && status.getKind() == SVNNodeKind.DIR);
                            //如果是删除
                            if (mynodeStatus.equals(SVNStatusType.STATUS_MISSING)){
                                svnNode.setStatus(SvnNode.STATUS_DELETE);
                                clientManager.getWCClient().doDelete(status.getFile(), true, false, false);
                            }else if(mynodeStatus.equals(SVNStatusType.STATUS_DELETED)) {
                                svnNode.setStatus(SvnNode.STATUS_DELETE);
                            } else if (mynodeStatus.equals(SVNStatusType.STATUS_UNVERSIONED)) {
                                svnNode.setStatus(SvnNode.STATUS_UNVERSIONED);
                                clientManager.getWCClient().doAdd(status.getFile(), true, false, false, SVNDepth.INFINITY, false, false);
                                if(svnNode.isDir()){
                                    addUnversionDirChildFiles(allList,file);
                                }
                            } else if (mynodeStatus.equals(SVNStatusType.STATUS_ADDED)) {//如果新增是新增
                                svnNode.setStatus(SvnNode.STATUS_ADD);
                            } else if (mynodeStatus.equals(SVNStatusType.STATUS_MODIFIED)) {//如果是修改
                                svnNode.setStatus(SvnNode.STATUS_MODIFY);
                            } else if (mynodeStatus.equals(SVNStatusType.STATUS_CONFLICTED)) {//如果是冲突
                                svnNode.setStatus(SvnNode.STATUS_CONFLICTED);
                            } else {
                                return;
                            }
                            allList.add(svnNode);
                        }
                    }, null);
                }
            }
            return allList;
        }finally {
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.getStatusClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }

    private void addUnversionDirChildFiles(List<SvnNode> list, File dir){
        File [] files = dir.listFiles();
        if(files.length > 0){
            for (File file : files) {
                SvnNode svnNode = new SvnNode();
                svnNode.setPath(file.getAbsolutePath());
                svnNode.setName(file.getName());
                svnNode.setParentPath(file.getParent());
                svnNode.setSize(file.length());
                svnNode.setSizeStr(DataSizeUtil.format(svnNode.getSize()));
                svnNode.setDate(new Date(file.lastModified()));
                svnNode.setDir(file.isDirectory());
                svnNode.setStatus(SvnNode.STATUS_UNVERSIONED);
                list.add(svnNode);
                if(svnNode.isDir()){
                    addUnversionDirChildFiles(list,file);
                }
            }
        }
    }

    /**
     * 提交指定文件，配合preCommitAll()方法使用
     *
     * @param filePath 提交文件路径
     * @return 提交结果
     * @throws SVNException svn异常
     */
    public SVNCommitInfo doCommitFiles(File [] filePath,String commitMessage, ISVNEventHandler handler) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            if (filePath != null && filePath.length > 0) {
                DoUpdateStatus = true;
                clientManager.getCommitClient().setEventHandler(handler);
                return clientManager.getCommitClient().doCommit(filePath, false, commitMessage, null, null, false, true, SVNDepth.EMPTY);
            }
        }finally {
            clientManager.getCommitClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
        return null;
    }

    /**
     * 全部提交svn
     *
     * @param filePath 待提交文件目录相对路径
     * @return 提交结果
     * @throws SVNException svn异常
     */
    public SVNCommitInfo doCommitAll(List<String> filePath,String commitMessage, ISVNEventHandler handler) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            if (filePath != null && filePath.size() > 0) {
                DoUpdateStatus = true;
                AtomicBoolean atomicBoolean = new AtomicBoolean(false);
                clientManager.getCommitClient().setEventHandler(handler);
                File [] files = filePath.stream().map(s->new File(localPath,s)).toArray(File[]::new);
                for (File file : files) {
                    clientManager.getStatusClient().doStatus(file, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, true, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) throws SVNException {
                            SVNStatusType mynodeStatus = status.getNodeStatus();
                            if (mynodeStatus.equals(SVNStatusType.STATUS_UNVERSIONED)) {
                                clientManager.getWCClient().doAdd(status.getFile(), true, false, false, SVNDepth.INFINITY, false, false);
                            }else if (mynodeStatus.equals(SVNStatusType.STATUS_MISSING)) {
                                clientManager.getWCClient().doDelete(status.getFile(), true, false, false);
                            }
                            if(!atomicBoolean.get()&&!mynodeStatus.equals(SVNStatusType.STATUS_NORMAL))
                                atomicBoolean.set(true);
                        }
                    }, null);
                }
                if(atomicBoolean.get())
                    return clientManager.getCommitClient().doCommit(files, false, commitMessage, null, null, false, true, SVNDepth.INFINITY);
            }
        }finally {
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.getCommitClient().getOperationsFactory().dispose();
            clientManager.getStatusClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
        return null;
    }

    /**
     * 本地是否有变更
     * @param ignores
     * @return
     * @throws SVNException
     */
    public boolean isChanged(String []ignores) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            String rootPath = new File(localPath,File.separator).getPath();
            File localFilePath = new File(localPath);
            clientManager.getStatusClient().doStatus(localFilePath, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, true, new ISVNStatusHandler() {
                @Override
                public void handleStatus(SVNStatus status) {
                    if(status.getNodeStatus() == SVNStatusType.STATUS_CONFLICTED ||(ignores == null || Arrays.stream(ignores).noneMatch(s->status.getFile().getPath().length() > rootPath.length() && status.getFile().getPath().substring(rootPath.length()).toLowerCase().indexOf(s.toLowerCase()) == 0))) {
                        throw new RuntimeException("");
                    }
                }
            },null);
        }catch (RuntimeException e){
            return true;
        }finally {
            clientManager.getStatusClient().getOperationsFactory().dispose();
            clientManager.dispose();
        }
        return false;
    }


    public boolean fileIsNormal(String filePath) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务，请稍后重试!");
        }
        try {
            if (filePath != null && filePath.length() > 0) {
                File file = new File(filePath);
                clientManager.getStatusClient().doStatus(file.getParentFile(), SVNRevision.WORKING, SVNDepth.FILES, false, false, false, true, new ISVNStatusHandler() {
                    @Override
                    public void handleStatus(SVNStatus status) {
                        if(status.getNodeStatus() == SVNStatusType.STATUS_NORMAL) {
                            return;
                        }else if(status.getFile().getName().equalsIgnoreCase(file.getName())) {
                            throw new RuntimeException("");
                        }
                    }
                }, null);
            }
        }catch (RuntimeException e){
            return false;
        } finally {
            clientManager.getStatusClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
        return true;
    }


    /**
     * 获取SVN信息
     *
     * @param filePath 文件夹路径相对路径
     * @param revision 版本选项 HEAD为服务器，WORKING本地目录
     * @return 执行结果
     */
    public SVNInfo doInfo(String filePath, SVNRevision revision) throws SVNException {
        SVNInfo info = null;
        try {
            File wcDir = new File(localPath,filePath);
            if (wcDir.exists()) {
                info = clientManager.getWCClient().doInfo(wcDir, revision);
            }
        } finally {
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.dispose();
        }
        return info;
    }

    /**
     * 获取二级目录是否变更
     * @return
     * @throws SVNException
     */
    public int isServerChanged() throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        AtomicInteger result = new AtomicInteger(0);
        try {
            File repoPath = new File(localPath);
            clientManager.getStatusClient().doStatus(repoPath, SVNRevision.HEAD, SVNDepth.IMMEDIATES, true, true, false, true, new ISVNStatusHandler() {
                @Override
                public void handleStatus(SVNStatus status) {
                    if(status.isConflicted()||status.isLocked()){
                        result.set(2);
                        throw new RuntimeException("");
                    }
                    if(!status.getFile().getPath().equals(repoPath.getPath()) && (status.getRemoteRevision() != SVNRevision.UNDEFINED||status.getRemoteNodeStatus() != SVNStatusType.STATUS_NONE)){
                        result.set(1);
                    }
                }
            }, null);
        } catch (RuntimeException e) {

        }finally {
            clientManager.getStatusClient().getOperationsFactory().dispose();
            clientManager.dispose();
        }
        return result.get();
    }

    /**
     * SVN仓库地址迁移
     * @param oldUrl
     * @param newUrl
     * @throws SVNException
     */
    public void doRelocate(SVNURL oldUrl,SVNURL newUrl) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        DoUpdateStatus = true;
        // 获得updateClient的实例
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        // 要更新的文件
        File updateFile = new File(localPath);
        updateClient.setIgnoreExternals(false);
        try {
            updateClient.doRelocate(updateFile,oldUrl,newUrl,true);
        }finally {
            clientManager.getUpdateClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }

    public InputStream getLatestFileStream(String path) throws SVNException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SVNNodeKind svnNodeKind = svnRepository.checkPath(path,-1);
        if(svnNodeKind == SVNNodeKind.NONE){
            return null;
        }
        svnRepository.getFile(path, -1, new SVNProperties(), outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public void doMkDir(List<String> path,String commitMessage) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            if (path != null && path.size() > 0) {
                DoUpdateStatus = true;
                SVNURL [] svnurls = new SVNURL[path.size()];
                for (int i = 0; i < path.size(); i++) {
                    svnurls[i] = SVNURL.parseURIEncoded(svnUrl + "/" + path.get(i));
                }
                clientManager.getCommitClient().doMkDir(svnurls,commitMessage,new SVNProperties(),true);
            }
        }finally {
            clientManager.getCommitClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }

    public void doDel(List<String> path,String commitMessage) throws SVNException {
        if (DoUpdateStatus) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            if (path != null && path.size() > 0) {
                DoUpdateStatus = true;
                SVNURL [] svnurls = new SVNURL[path.size()];
                for (int i = 0; i < path.size(); i++) {
                    svnurls[i] = SVNURL.parseURIEncoded(svnUrl + "/" + path.get(i));
                }
                clientManager.getCommitClient().doDelete(svnurls,commitMessage);
            }
        }finally {
            clientManager.getCommitClient().getOperationsFactory().dispose();
            clientManager.dispose();
            DoUpdateStatus = false;
        }
    }
}


