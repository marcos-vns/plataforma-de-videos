package model;
import java.util.ArrayList;

public class Channel {
	private long id;
	private String name;
	private long subscribers;
	private String profilePictureUrl;
	
	private ArrayList<UserChannel> roles;
	private Role currentUserRole;
	
	public Channel(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Channel(long id, String name, String profilePictureUrl) {
		this.id = id;
		this.name = name;
		this.profilePictureUrl = profilePictureUrl;
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

	public long getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(long subscribers) {
		this.subscribers = subscribers;
	}

	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}

	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}

	public Role getCurrentUserRole() {
		return currentUserRole;
	}

	public void setCurrentUserRole(Role currentUserRole) {
		this.currentUserRole = currentUserRole;
	}

	@Override
	public String toString() {
		return name;
	}
}
