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
	private String password;
	private Role role;

	private List<UserChannel> participations;
	
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Role getRole() {
		return role;
	}
	public Role setRole(Role role) {
		this.role = role;
	}

	public void createChannel(Connection conn, String channelName) {
		
		ChannelDAO channelDAO = new ChannelDAO();
		Channel channel = channelDAO.save(conn, channelName);
		
		UserChannelDAO userChannelDAO = new UserChannelDAO();
		UserChannel participation = new UserChannel();
		participation.setChannel(channel);
		participation.setUser(this);
		participation.setRole(Role.OWNER);
		
		userChannelDAO.save(conn, participation);
		
	}

}
