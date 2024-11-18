package com.lightframework.comm.svn;

import cn.hutool.core.io.FileUtil;
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

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SvnClient {

    private Logger logger = LoggerFactory.getLogger(SvnClient.class);

    private final ReentrantLock lock = new ReentrantLock();

    // 声明SVN客户端管理类
    private SVNClientManager clientManager;

    // 声明SVN服务端仓储对象
    private SVNRepository svnRepository;

    private String svnUrl;

    private String localPath;

    private String username;
    private String password;

    private SvnClient(){}

    public SvnClient(String svnUrl, String localPath, String userName, String password){
        this.svnUrl = svnUrl;
        this.localPath = localPath;
        this.username = userName;
        this.password = password;
        initClientAuthSvn(svnUrl,userName,password);
    }

    public ReentrantLock getLock(){
        return this.lock;
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
        return lock.isLocked();
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

    public void checkout(ISVNEventHandler handler,boolean allowUnversionedObstructions) throws SVNException {
        checkout(SVNRevision.HEAD,handler,allowUnversionedObstructions);
    }

    /**
     * svn检出
     *
     * @return 检出结果
     * @throws SVNException svn处理异常
     */
    public void checkout(SVNRevision revision,ISVNEventHandler handler,boolean allowUnversionedObstructions) throws SVNException {
        try {
            lock.lock();
            // 相关变量赋值
            SVNURL repositoryURL = SVNURL.parseURIEncoded(svnUrl);
            // 要把版本库的内容check out到的目录
            File wcDir = new File(localPath);
            // 通过客户端管理类获得updateClient类的实例。
            SVNUpdateClient updateClient = clientManager.getUpdateClient();
            updateClient.setIgnoreExternals(false);
            // 执行check out 操作，返回工作副本的版本号。
            updateClient.setEventHandler(handler);
            updateClient.doCheckout(repositoryURL, wcDir, revision, revision, SVNDepth.INFINITY, allowUnversionedObstructions);
        } finally {
            clientManager.getUpdateClient().getOperationsFactory().dispose();
            clientManager.dispose();
            lock.unlock();
        }
    }

    /**
     * 获取两个版本差异
     * @param filePath 文件相对路径
     * @param fromVersion
     * @param toVersion
     * @return
     * @throws SVNException
     */
    public List<SvnNode> doCompareRevision(String filePath,long fromVersion,long toVersion) throws SVNException {
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            File repoPath = new File(localPath,filePath);
            String parentPath = repoPath.getParent();
            if(filePath.length() == 0 || filePath.equals(File.separator)){
                parentPath = repoPath.getPath();
            }
            File parent = new File(parentPath);
            List<SvnNode> svnTrees = new ArrayList<>();
            clientManager.getDiffClient().doDiffStatus(repoPath, SVNRevision.create(fromVersion), repoPath, SVNRevision.create(toVersion), SVNDepth.INFINITY, false, new ISVNDiffStatusHandler() {
                @Override
                public void handleDiffStatus(SVNDiffStatus svnDiffStatus) throws SVNException {
                    File file = new File(parent,svnDiffStatus.getPath());
                    SvnNode svnNode = new SvnNode();
                    svnNode.setPath(file.getPath());
                    svnNode.setName(file.getName());
                    svnNode.setParentPath(file.getParent());
                    svnNode.setDir(svnDiffStatus.getKind() == SVNNodeKind.DIR);
                    if (svnDiffStatus.getModificationType() == SVNStatusType.STATUS_DELETED) {
                        svnNode.setStatus(SvnNode.STATUS_DELETE);
                    }else if (svnDiffStatus.getModificationType() == SVNStatusType.STATUS_ADDED) {//如果新增是新增
                        svnNode.setStatus(SvnNode.STATUS_ADD);
                    }else if (svnDiffStatus.getModificationType() == SVNStatusType.STATUS_MODIFIED) {//如果是修改
                        svnNode.setStatus(SvnNode.STATUS_MODIFY);
                    }else{
                        return;
                    }
                    svnTrees.add(svnNode);
                }
            });
            return svnTrees;
        } finally {
            clientManager.getDiffClient().getOperationsFactory().dispose();
            clientManager.dispose();
            lock.unlock();
        }
    }

    /**
     * 清理目录下的svn信息
     *
     * @param removeUnversionedItems 是否删除未被版本控制的文件
     * @return 执行结果
     */
    public boolean doCleanup(boolean removeUnversionedItems, ISVNEventHandler handler) throws SVNException {
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
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
            lock.unlock();
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
        if (filePath != null && filePath.size() > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                String rootPath = new File(localPath, File.separator).getPath();
                clientManager.getWCClient().setEventHandler(handler);
                List<File> allList = new ArrayList<>();
                for (String path : filePath) {
                    File file = new File(localPath, path);
                    clientManager.getStatusClient().doStatus(file, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) throws SVNException {
                            if (status.getNodeStatus() == SVNStatusType.STATUS_CONFLICTED || (ignores == null || Arrays.stream(ignores).noneMatch(s -> status.getFile().getPath().length() > rootPath.length() && status.getFile().getPath().substring(rootPath.length()).toLowerCase().indexOf(s.toLowerCase()) == 0))) {
                                SVNStatusType mynodeStatus = status.getNodeStatus();
                                if (mynodeStatus.equals(SVNStatusType.STATUS_UNVERSIONED)) {
                                    if(deleteAddFile) {
                                        delete(status.getFile(), rootPath, ignores, handler);
                                    }
                                } else if (mynodeStatus.equals(SVNStatusType.STATUS_ADDED)) {
                                    clientManager.getWCClient().doDelete(status.getFile(), true, true, false);
                                } else {
                                    allList.add(status.getFile());
                                }
                            }
                        }
                    }, null);
                }
                if (allList.size() > 0) {
                    clientManager.getWCClient().doRevert(allList.stream().toArray(File[]::new), SVNDepth.INFINITY, null);
                }
            } finally {
                clientManager.getStatusClient().getOperationsFactory().dispose();
                clientManager.getWCClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
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

    private boolean delete(File file,String rootPath,String []ignores, ISVNEventHandler handler) throws SVNException {
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
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
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
            lock.unlock();
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
     * @param svnPath      远程根路径
     * @param srcPath     文件所在文件夹的路径
     * @param isRecursive 是否递归
     * @throws SVNException svn异常
     */
    public void doImport(String svnPath, String srcPath, boolean isRecursive, String commitMessage) throws SVNException {
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            SVNURL repositoryURL = SVNURL.parseURIEncoded(svnUrl +(svnPath == null || svnPath.isEmpty()?"":(svnPath.startsWith("/")?"":"/")+ svnPath));
            // 要把此目录中的内容导入到版本库
            File impDir = new File(srcPath);
            // 执行导入操作
            clientManager.getCommitClient().doImport(impDir, repositoryURL,
                    commitMessage, null,
                    true, true,
                    SVNDepth.fromRecurse(isRecursive));
        }finally {
            clientManager.getCommitClient().getOperationsFactory().dispose();
            clientManager.dispose();
            lock.unlock();
        }
    }

    public void doExport(String svnPath, String targetPath,long reversion) throws SVNException {
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            SVNURL repositoryURL = SVNURL.parseURIEncoded(svnUrl +(svnPath == null || svnPath.isEmpty()?"":(svnPath.startsWith("/")?"":"/")+ svnPath));
            // 要把此目录中的内容导入到版本库
            File targetFile = new File(targetPath);
            clientManager.getUpdateClient().doExport(repositoryURL,targetFile,SVNRevision.create(reversion),SVNRevision.create(reversion),null,true,SVNDepth.INFINITY);
        }finally {
            clientManager.getUpdateClient().getOperationsFactory().dispose();
            clientManager.dispose();
            lock.unlock();
        }
    }

    public SVNCommitInfo doRollback(long reversion) throws SVNException {
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            if(!isWorkingCopy()) {
                throw new RuntimeException(localPath+" is not workingCopy!");
            }
            cleanLocalRepository(new File(localPath));
            doExport("", localPath, reversion);
            return doCommitAll(Arrays.asList(""), "rollback to revision: " + reversion, null);
        }finally {
            lock.unlock();
        }
    }

    private void cleanLocalRepository(File repositoryFile){
        //清空本地程序目录
        for (File listFile : repositoryFile.listFiles()) {
            if(listFile.getName().equals(".svn") && listFile.isDirectory()){
                continue;
            }
            FileUtil.del(listFile);
        }
    }

    public boolean checkDirExist(String svnUrl,long revision) throws SVNException {
        try {
            Collection collection = svnRepository.getDir(svnUrl,revision,null,(Collection) null);
            return true;
        } catch (SVNException e) {
            if(e.getErrorMessage().getErrorCode().getCode() == SVNErrorCode.FS_NOT_FOUND.getCode()
                    ||e.getErrorMessage().getErrorCode().getCode() == SVNErrorCode.FS_NOT_DIRECTORY.getCode()){
                return false;
            }
            throw e;
        }
    }

    /**
     * 预提交,并返回提交列表
     *
     * @param filePath 待提交文件目录相对路径
     * @return 提交结果
     * @throws SVNException svn异常
     */
    public List<SvnNode> preCommitAll(List<String> filePath) throws SVNException {
        List<SvnNode> allList = new ArrayList<>();
        if (filePath != null && filePath.size() > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                for (String path : filePath) {
                    File file = new File(localPath, path);
                    clientManager.getStatusClient().doStatus(file, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
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
                            svnNode.setDir(mynodeStatus == SVNStatusType.STATUS_UNVERSIONED ? file.isDirectory() : status.getKind() != null && status.getKind() == SVNNodeKind.DIR);
                            //如果是删除
                            if (mynodeStatus.equals(SVNStatusType.STATUS_MISSING)) {
                                svnNode.setStatus(SvnNode.STATUS_DELETE);
                                clientManager.getWCClient().doDelete(status.getFile(), true, false, false);
                            } else if (mynodeStatus.equals(SVNStatusType.STATUS_DELETED)) {
                                svnNode.setStatus(SvnNode.STATUS_DELETE);
                            } else if (mynodeStatus.equals(SVNStatusType.STATUS_UNVERSIONED)) {
                                try {
                                    if(!file.getPath().equals(file.getCanonicalPath())){
                                        return;
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                svnNode.setStatus(SvnNode.STATUS_UNVERSIONED);
                                clientManager.getWCClient().doAdd(status.getFile(), true, false, false, SVNDepth.INFINITY, false, false);
                                if (svnNode.isDir()) {
                                    addUnversionDirChildFiles(allList, file);
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
            } finally {
                clientManager.getWCClient().getOperationsFactory().dispose();
                clientManager.getStatusClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
        }
        return allList;
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
        if (filePath != null && filePath.length > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                clientManager.getCommitClient().setEventHandler(handler);
                return clientManager.getCommitClient().doCommit(filePath, false, commitMessage, null, null, false, true, SVNDepth.EMPTY);
            } finally {
                clientManager.getCommitClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
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
        if (filePath != null && filePath.size() > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                AtomicBoolean atomicBoolean = new AtomicBoolean(false);
                clientManager.getCommitClient().setEventHandler(handler);
                File[] files = filePath.stream().map(s -> new File(localPath, s)).toArray(File[]::new);
                for (File file : files) {
                    clientManager.getStatusClient().doStatus(file, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) throws SVNException {
                            SVNStatusType mynodeStatus = status.getNodeStatus();
                            if (mynodeStatus.equals(SVNStatusType.STATUS_UNVERSIONED)) {
                                try {
                                    if(!status.getFile().getPath().equals(status.getFile().getCanonicalPath())){
                                        return;
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                clientManager.getWCClient().doAdd(status.getFile(), true, false, false, SVNDepth.INFINITY, false, false);
                            } else if (mynodeStatus.equals(SVNStatusType.STATUS_MISSING)) {
                                clientManager.getWCClient().doDelete(status.getFile(), true, false, false);
                            }
                            if (!atomicBoolean.get() && !mynodeStatus.equals(SVNStatusType.STATUS_NORMAL))
                                atomicBoolean.set(true);
                        }
                    }, null);
                }
                if (atomicBoolean.get()) {
                    return clientManager.getCommitClient().doCommit(files, false, commitMessage, null, null, false, true, SVNDepth.INFINITY);
                }
            } finally {
                clientManager.getWCClient().getOperationsFactory().dispose();
                clientManager.getCommitClient().getOperationsFactory().dispose();
                clientManager.getStatusClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
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
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            String rootPath = new File(localPath,File.separator).getPath();
            File localFilePath = new File(localPath);
            clientManager.getStatusClient().doStatus(localFilePath, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
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
            lock.unlock();
        }
        return false;
    }


    public boolean fileIsNormal(String filePath) throws SVNException {
        if (filePath != null && filePath.length() > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                File file = new File(localPath,filePath);
                clientManager.getStatusClient().doStatus(file.getParentFile(), SVNRevision.WORKING, SVNDepth.FILES, false, false, false, false, new ISVNStatusHandler() {
                    @Override
                    public void handleStatus(SVNStatus status) {
                        if (status.getNodeStatus() == SVNStatusType.STATUS_NORMAL) {
                            return;
                        } else if (status.getFile().getName().equalsIgnoreCase(file.getName())) {
                            throw new RuntimeException("");
                        }
                    }
                }, null);
            } catch (RuntimeException e) {
                return false;
            } finally {
                clientManager.getStatusClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
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
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            File wcDir = new File(localPath,filePath);
            if (wcDir.exists()) {
                return clientManager.getWCClient().doInfo(wcDir, revision);
            }
        } finally {
            clientManager.getWCClient().getOperationsFactory().dispose();
            clientManager.dispose();
            lock.unlock();
        }
        return null;
    }

    /**
     * 获取二级目录是否变更
     * @return
     * @throws SVNException
     */
    public int isServerChanged() throws SVNException {
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        AtomicInteger result = new AtomicInteger(0);
        try {
            File repoPath = new File(localPath);
            clientManager.getStatusClient().doStatus(repoPath, SVNRevision.HEAD, SVNDepth.IMMEDIATES, true, true, false, false, new ISVNStatusHandler() {
                @Override
                public void handleStatus(SVNStatus status) {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
            lock.unlock();
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
        if (!lock.tryLock()) {
            throw new RuntimeException("正在执行其它任务!");
        }
        try {
            // 获得updateClient的实例
            SVNUpdateClient updateClient = clientManager.getUpdateClient();
            // 要更新的文件
            File updateFile = new File(localPath);
            updateClient.setIgnoreExternals(false);
            updateClient.doRelocate(updateFile,oldUrl,newUrl,true);
            this.svnUrl = newUrl.toDecodedString();
            initClientAuthSvn(svnUrl,username,password);
        }finally {
            clientManager.getUpdateClient().getOperationsFactory().dispose();
            clientManager.dispose();
            lock.unlock();
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
        if (path != null && path.size() > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                SVNURL[] svnurls = new SVNURL[path.size()];
                for (int i = 0; i < path.size(); i++) {
                    svnurls[i] = SVNURL.parseURIEncoded(svnUrl + (path.get(i).startsWith("/")?"":"/") + path.get(i));
                }
                clientManager.getCommitClient().doMkDir(svnurls, commitMessage, new SVNProperties(), true);
            } finally {
                clientManager.getCommitClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
        }
    }

    public void doDel(List<String> path,String commitMessage) throws SVNException {
        if (path != null && path.size() > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                SVNURL[] svnurls = new SVNURL[path.size()];
                for (int i = 0; i < path.size(); i++) {
                    svnurls[i] = SVNURL.parseURIEncoded(svnUrl + (path.get(i).startsWith("/")?"":"/") + path.get(i));
                }
                clientManager.getCommitClient().doDelete(svnurls, commitMessage);
            } finally {
                clientManager.getCommitClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
        }
    }

    /**
     * 获取本地变更列表
     * @param filePath
     * @param ignores
     * @return
     * @throws SVNException
     */
    public List<SvnNode> getLocalStatus(List<String> filePath,String [] ignores) throws SVNException {
        if (filePath != null && filePath.size() > 0) {
            if (!lock.tryLock()) {
                throw new RuntimeException("正在执行其它任务!");
            }
            try {
                String rootPath = new File(localPath, File.separator).getPath();
                // 获取此文件的状态（是文件做了修改还是新添加的文件？）
                List<SvnNode> allList = new ArrayList<>();
                for (String path : filePath) {
                    File file = new File(localPath, path);
                    clientManager.getStatusClient().doStatus(file, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, true, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) {
                            if (status.getNodeStatus() == SVNStatusType.STATUS_NORMAL) {
                                return;
                            }
                            //排除掉文件冲突后生成的文件
                            if (status.getNodeStatus() == SVNStatusType.STATUS_CONFLICTED || ((ignores == null || Arrays.stream(ignores).noneMatch(s -> status.getFile().getPath().length() > rootPath.length() && status.getFile().getPath().substring(rootPath.length()).toLowerCase().indexOf(s.toLowerCase()) == 0))
                                    && (!status.getNodeStatus().equals(SVNStatusType.STATUS_UNVERSIONED) || allList.stream().noneMatch(v -> v.getStatus().equals(SvnNode.STATUS_CONFLICTED) && status.getFile().getName().contains(v.getName() + "."))))) {
                                File file = status.getFile();
                                SvnNode svnNode = new SvnNode();
                                svnNode.setPath(file.getAbsolutePath());
                                svnNode.setName(file.getName());
                                svnNode.setParentPath(file.getParent());
                                svnNode.setSize(file.length());
                                svnNode.setSizeStr(DataSizeUtil.format(svnNode.getSize()));
                                svnNode.setDate(new Date(file.lastModified()));
                                SVNStatusType mynodeStatus = status.getNodeStatus();
                                svnNode.setDir(mynodeStatus == SVNStatusType.STATUS_UNVERSIONED ? file.isDirectory() : status.getKind() != null && status.getKind() == SVNNodeKind.DIR);
                                //如果是删除
                                if (mynodeStatus.equals(SVNStatusType.STATUS_MISSING) || mynodeStatus.equals(SVNStatusType.STATUS_DELETED)) {
                                    svnNode.setStatus(SvnNode.STATUS_DELETE);
                                } else if (mynodeStatus.equals(SVNStatusType.STATUS_UNVERSIONED)) {
                                    svnNode.setStatus(SvnNode.STATUS_UNVERSIONED);
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
                        }
                    }, null);
                }
                return allList;
            } finally {
                clientManager.getStatusClient().getOperationsFactory().dispose();
                clientManager.dispose();
                lock.unlock();
            }
        }
        return null;
    }

    /**
     * 获取本地更新
     * @return 树形结构
     */
    public List<SvnNode> getLocalModify(String relativePath) throws SVNException {
        String svnPath = new File(localPath,relativePath).getPath();
        List<SvnNode> localStatus = getLocalStatus(Arrays.asList(relativePath),null);
        List<SvnNode> svnTrees = new ArrayList<>();
        Map<String,SvnNode> searchMap = new HashMap<>(); //利用map数据结构以及搜索的优势快速查找定位指定路径SvnTree对象
        SortedMap<String,List<SvnNode>> mergeMap = new TreeMap<>(new Comparator<String>() {//合并为TreeMap对象，保证路径最短最先处理
            @Override
            public int compare(String o1, String o2) {
                int r = o1.length() - o2.length();
                if(r == 0){
                    return o1.compareTo(o2);
                }
                return r;
            }
        });
        this.mergeMap(mergeMap,localStatus,svnPath);
        for(Map.Entry<String,List<SvnNode>> entry:mergeMap.entrySet()){ //遍历本地变更的内容，创建父级SvnTree并维护树状结构
            String path = entry.getKey();
            List<SvnNode> child = entry.getValue();
            for(SvnNode tree:child){
                if(tree.isDir()){
                    searchMap.put(("".equals(path)?"":path + File.separator) + tree.getName(),tree);
                }
            }
            if("".equals(path)){
                svnTrees = child;
                continue;
            }
            SvnNode svnNode;
            while (true) {
                if ((svnNode = searchMap.get(path)) == null) { //文件路径反向遍历
                    File file = new File(svnPath, path);
                    svnNode = new SvnNode();
                    svnNode.setName(file.getName());
                    svnNode.setDir(file.isDirectory());
                    svnNode.setPath(file.getAbsolutePath());
                    svnNode.setSize(file.length());
                    svnNode.setSizeStr(DataSizeUtil.format(svnNode.getSize()));
                    svnNode.setDate(new Date(file.lastModified()));
                    svnNode.setStatus(SvnNode.STATUS_MODIFY);
                    svnNode.setChild(child);
                    searchMap.put(path, svnNode);
                    int sepIndex = path.lastIndexOf(File.separator);
                    if (sepIndex > 0) {
                        child = new ArrayList<>();
                        child.add(svnNode);
                        path = path.substring(0, sepIndex);
                    } else {
                        svnTrees.add(svnNode);
                        break;
                    }
                } else {
                    if (svnNode.getChild() == null) {
                        svnNode.setChild(child);
                    } else {
                        svnNode.getChild().addAll(child);
                    }
                    break;
                }
            }
        }
        SvnNode.sort(svnTrees);
        return svnTrees;
    }

    /**
     * 将本地修改的文件创建为SvnTree对象，并合并路径
     * @param map
     * @param list
     */
    private void mergeMap(SortedMap<String,List<SvnNode>> map,List<SvnNode> list,String svnPath){
        if(list != null && !list.isEmpty()) {
            File localFile = new File(svnPath);
            int rootIndex = localFile.getAbsolutePath().length() + 1;
            list.forEach(file -> {
                String path = file.getPath();
                if(!file.equals(localFile.getAbsolutePath())) {
                    int sepIndex = path.lastIndexOf(File.separator);
                    String key = sepIndex > rootIndex ? path.substring(rootIndex, sepIndex) : "";
                    if(file.getStatus().equals(SvnNode.STATUS_UNVERSIONED)){
                        file.setStatus(SvnNode.STATUS_ADD);
                        if(file.isDir()){
                            file.setChild(getFileTree(new File(path),SvnNode.STATUS_ADD));
                        }
                    }
                    List<SvnNode> svnFiles;
                    if ((svnFiles = map.get(key)) == null) {
                        svnFiles = new ArrayList<>();
                        map.put(key, svnFiles);
                    }
                    svnFiles.add(file);
                }
            });
        }
    }

    public List<SvnNode> getFileTree(File file) {
        return this.getFileTree(file,null);
    }

    public List<SvnNode> getFileTree(File file,String status) {
        List<SvnNode> baseTreeNodes = new ArrayList<>();
        File[] childFiles = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !".svn".equalsIgnoreCase(name);
            }
        });
        if (childFiles != null) {
            for (File listFile : childFiles) {
                SvnNode svnNode = new SvnNode();
                svnNode.setName(listFile.getName());
                svnNode.setDir(listFile.isDirectory());
                svnNode.setPath(listFile.getAbsolutePath());
                svnNode.setSize(listFile.length());
                svnNode.setSizeStr(DataSizeUtil.format(svnNode.getSize()));
                svnNode.setDate(new Date(listFile.lastModified()));
                svnNode.setStatus(status);
                svnNode.setChild(getFileTree(listFile,status));
                baseTreeNodes.add(svnNode);
            }
        }
        return baseTreeNodes;
    }
}


