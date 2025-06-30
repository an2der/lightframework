package com.lightframework.comm.svn;


import com.lightframework.common.LightException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SvnNode implements Comparable<SvnNode>,Cloneable {

	public static final String STATUS_NORMAL = "normal";
	public static final String STATUS_ADD = "add";
	public static final String STATUS_MODIFY = "modify";
	public static final String STATUS_DELETE = "delete";
	public static final String STATUS_UNVERSIONED = "unversioned";
	public static final String STATUS_CONFLICTED = "conflicted";

	private String name;

	private boolean isDir;

	private long revision;

	private String author;

	private Date date;
	private long size;
	private String sizeStr;

	private String path;

	private String parentPath;

	private String relativePath;

	private String jumpPath;

	/**
	 * normal、add、modify、delete
	 */
	private String status = STATUS_NORMAL;

	private SvnNode parent;

	private List<SvnNode> child;

	public void setStatus(String status){
		this.status = status;
		if(parent != null && STATUS_NORMAL.equals(parent.getStatus())){
			parent.setStatus(STATUS_MODIFY);
		}
	}

	public void addChild(SvnNode tree){
		if(child == null){
			child = new ArrayList<>();
		}
		child.add(tree);
	}

	@Override
	public int compareTo(SvnNode o) {
		if(this.isDir == o.isDir){
			return this.getName().compareToIgnoreCase(o.getName());
		}else if(this.isDir){
			return -1;
		}
		return 1;
	}

	public static void sort(List<SvnNode> trees){
		if(trees != null) {
			if (trees.size() > 1) {
				Collections.sort(trees);
			}
			for (SvnNode svnNode : trees) {
				sort(svnNode.getChild());
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDir() {
		return isDir;
	}

	public void setDir(boolean dir) {
		isDir = dir;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getSizeStr() {
		return sizeStr;
	}

	public void setSizeStr(String sizeStr) {
		this.sizeStr = sizeStr;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getJumpPath() {
		return jumpPath;
	}

	public void setJumpPath(String jumpPath) {
		this.jumpPath = jumpPath;
	}

	public String getStatus() {
		return status;
	}

	public SvnNode getParent() {
		return parent;
	}

	public void setParent(SvnNode parent) {
		this.parent = parent;
	}

	public List<SvnNode> getChild() {
		return child;
	}

	public void setChild(List<SvnNode> child) {
		this.child = child;
	}

	@Override
	public SvnNode clone() {
		try {
			return (SvnNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new LightException("调用SvnNode对象clone方法发生异常");
		}
	}
}
