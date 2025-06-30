package com.lightframework.auth.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 用户信息实体
 * @author yg
 * @date 2022/6/13 9:22
 * @version 1.0
 */
public class UserInfo implements Serializable{

	public static final short MAN = 1;
	public static final short WOMAN = 0;

	private static final long serialVersionUID = 6818738597004272801L;

	private String accessToken;
	
	private String userId;
	private String username;

	private transient String password;
	private transient String salt;

	private String realName;
	private Short age;
	private Short gender;
	private String phone;
	private String email;
	private String address;
	private String headImg;
	private boolean isAdmin = false;
	private boolean isEnabled = true;
	private List<IdName> roles = new ArrayList<>();
	private List<IdName> departments = new ArrayList<>();
	private Set<String> permissions;
	private Map<String,Object> others;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Short getAge() {
		return age;
	}

	public void setAge(Short age) {
		this.age = age;
	}

	public Short getGender() {
		return gender;
	}

	public void setGender(Short gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getHeadImg() {
		return headImg;
	}

	public void setHeadImg(String headImg) {
		this.headImg = headImg;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean admin) {
		isAdmin = admin;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}

	public List<IdName> getRoles() {
		return roles;
	}

	public void setRoles(List<IdName> roles) {
		this.roles = roles;
	}

	public List<IdName> getDepartments() {
		return departments;
	}

	public void setDepartments(List<IdName> departments) {
		this.departments = departments;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	public Map<String, Object> getOthers() {
		return others;
	}

	public void setOthers(Map<String, Object> others) {
		this.others = others;
	}

	public String password() {
		return password;
	}

	public String salt(){
		return salt;
	}

	public static class IdName {
		private String id;
		private String name;

		public IdName(String id, String name) {
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
