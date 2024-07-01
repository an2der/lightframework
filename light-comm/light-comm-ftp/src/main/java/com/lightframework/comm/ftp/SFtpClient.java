package com.lightframework.comm.ftp;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.lightframework.common.BusinessException;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/***
 * @author yg
 * @date 2023/8/21 11:09
 * @version 1.0
 */
public class SFtpClient extends BaseFtp {

	private Session session = null;
	private ChannelSftp channel = null;

	public SFtpClient(String host, String username, String password, String serverName) {
		super(host, Ftp.SFTP_PORT, username, password, serverName);
	}

	public SFtpClient(String host, String username, String password, String serverName, String rootPath) {
		super(host, Ftp.SFTP_PORT, username, password, serverName, rootPath);
	}

	public SFtpClient(String host, int port, String username, String password, String serverName, String rootPath) {
		super(host, port, username, password, serverName, rootPath);
	}

	public SFtpClient(String host, String username, String password, String serverName, String rootPath, String encoding) {
		super(host, Ftp.SFTP_PORT, username, password, serverName, rootPath, encoding);
	}

	public SFtpClient(String host,int port, String username, String password, String serverName, String rootPath, String encoding) {
		super(host, port, username, password, serverName, rootPath, encoding);
	}

	public SFtpClient(String host, int port, String username, String password, String serverName, String rootPath, String encoding, int connectTimeout) {
		super(host, port, username, password, serverName, rootPath, encoding, connectTimeout);
	}

	@Override
	public SFtpClient getNewInstance() {
		return new SFtpClient(this.host,this.port,this.username,this.password,this.serverName,this.rootPath,this.encode,this.connectTimeout);
	}

	@Override
	public boolean connect() {
		try {
			JSch jSch = new JSch();
			session = jSch.getSession(username, host, port);
			if (password != null) {
				session.setPassword(password);
			}
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setTimeout(connectTimeout);
			session.connect();

			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			if(rootPath != null && !rootPath.isEmpty()){
				this.changeDir(rootPath);
			}
			return true;
		} catch (Exception e) {
			throw new BusinessException("sftp 服务器登录失败");
		}
	}

	@Override
	public void close() {
		if (channel != null) {
			channel.quit();
			channel.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}

	@Override
	public boolean upload(String filepath, String filename, InputStream inputStream) {
		try {
			channel.put(inputStream, filepath + separator + filename, ChannelSftp.OVERWRITE);
			return validateRemoteFileExist(filepath,filename,false);
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public boolean download(String remotePath, String fileName, String localPath, String localName) {
		try {
			String remoteFile = remotePath + separator + fileName;
			File localFile = new File(localPath,localName);
			channel.get(remoteFile, localFile.getPath());
			return localFile.exists();
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public boolean rename(String oldPath, String newPath) {
		try {
			channel.rename(oldPath, newPath);
			return true;
		} catch (Exception e) {
			if(!SftpException.class.isInstance(e) || e.getCause() != null) {
				throw new BusinessException(e);
			}
		}
		return false;
	}

	@Override
	public boolean remove(String filepath) {
		try {
			channel.rm(filepath);
			return true;
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public boolean removeDir(String path) {
		try {
			Vector<ChannelSftp.LsEntry> lsEntries = channel.ls(path);
			for (ChannelSftp.LsEntry lsEntry : lsEntries) {
				String filePath = path + separator + lsEntry.getFilename();
				if (!lsEntry.getFilename().equals(".") && !lsEntry.getFilename().equals("..")) {
					if (lsEntry.getAttrs().isDir()) {
						removeDir(filePath);
					} else {
						channel.rm(rootPath + separator + filePath);
					}
				}
			}
			channel.rmdir(path);
			return true;
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public List<FtpNode> list(String path) {
		try {
			Vector<ChannelSftp.LsEntry> list = channel.ls(path);
			String parent = path.lastIndexOf(separator) == path.length() - 1?path.substring(0,path.length() - 1):path;
			return list.stream().filter(lsEntry -> !lsEntry.getFilename().equals(".") && !lsEntry.getFilename().equals(".."))
					.sorted(Comparator.comparing(ChannelSftp.LsEntry::getFilename,String.CASE_INSENSITIVE_ORDER))
					.sorted(Comparator.comparing(lsEntry -> lsEntry.getAttrs().isDir(), Comparator.reverseOrder())).map(f ->
					new FtpNode() {{
						setName(f.getFilename().trim());
						setParentPath(parent);
						setPath(getParentPath() + "/" + getName());
						setCreateTime(new Date(f.getAttrs().getATime()*1000L));
						setModifyTime(new Date(f.getAttrs().getMTime()*1000L));
						setSize(f.getAttrs().getSize());
						setDirectory(f.getAttrs().isDir());
					}}
			).collect(Collectors.toList());
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public void mkdir(String path) {
		try {
			int lastSepIndex = path.lastIndexOf(separator);
			if(changeDir(path.substring(0,lastSepIndex))){
				channel.mkdir(path.substring(lastSepIndex+1));
			}else {
				for (String d : path.split(separator)) {
					if (d != null && !d.trim().isEmpty() && !changeDir(d)) {
						channel.mkdir(d);
						channel.cd(d);
					}
				}
			}
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public boolean validateRemoteFileExist(String path, String fileName,boolean isDir) {
		try {
			Vector<ChannelSftp.LsEntry> list = channel.ls(path);
			for (ChannelSftp.LsEntry lsEntry : list) {
				if(lsEntry.getFilename().equals(fileName) && (isDir?lsEntry.getAttrs().isDir():!lsEntry.getAttrs().isDir())){
					return true;
				}
			}
		}catch (Exception e) {
			if(!SftpException.class.isInstance(e) || e.getCause() != null) {
				throw new BusinessException(e);
			}
		}
		return false;
	}

	@Override
	public boolean changeDir(String path) {
		try {
			channel.cd(path);
			return true;
		} catch (Exception e) {
			if(!SftpException.class.isInstance(e) || e.getCause() != null) {
				throw new BusinessException(e);
			}
		}
		return false;
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public String currentDir() {
		try {
			return channel.pwd();
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}

	@Override
	public boolean getStatus() {
		try {
			channel.pwd();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private ChannelSftp.LsEntry getEntry(String path,String fileName){
		try {
			Vector<ChannelSftp.LsEntry> list = channel.ls(path);
			for (ChannelSftp.LsEntry lsEntry : list) {
				if(lsEntry.getFilename().equals(fileName)){
					return lsEntry;
				}
			}
		}catch (SftpException e){

		}
		return null;
	}

}


