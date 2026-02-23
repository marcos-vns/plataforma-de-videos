package model;
import java.sql.Connection;
import java.util.List;

import daos.ChannelDAO;
import daos.UserChannelDAO;

public class User{
	private long id;
	private String name;
	private String username;
	private String email;
	private String passwordHash;
	private String profilePictureUrl;
	private Role role;

	private List<UserChannel> participations;
	
	public User(long id, String email, String username, String name, String password) {
		this.id = id;
		this.email = email;
		this.username = username;
		this.passwordHash = password;
		this.name = name;
	}

	public User(long id, String email, String username, String name, String password, String profilePictureUrl) {
		this.id = id;
		this.email = email;
		this.passwordHash = password;
		this.name = name;
		this.username = username;
		this.profilePictureUrl = profilePictureUrl;
	}
	
	public User(String email, String password, String username, String name) {
		this.email = email;
		this.passwordHash = password;
		this.username = username;
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public void setPassword(String password) {
		this.passwordHash = password;
	}
	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}
	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}

}
