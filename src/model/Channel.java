package model;
import java.util.ArrayList;

public class Channel {
	private long id;
	private String name;
	private long subscribers;
	
	private ArrayList<UserChannel> cargos;
	
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
	
	
}
