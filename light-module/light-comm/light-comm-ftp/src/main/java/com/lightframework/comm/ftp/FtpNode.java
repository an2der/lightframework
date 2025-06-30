package com.lightframework.comm.ftp;

import lombok.Data;

import java.util.Date;
import java.util.List;

/** FTP MODEL
 * @author yg
 * @date 2022/4/26 10:55
 * @version 1.0
 */
@Data
public class FtpNode {

	private String name;
	private String path;
	private Date createTime;//创建时间
	private Date modifyTime;//修改时间
	private long size;
	private boolean isDirectory;
	private String parentPath;
	private List<FtpNode> childList;


}
