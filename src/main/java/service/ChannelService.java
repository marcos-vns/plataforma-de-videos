package service;

import java.util.List;

import daos.ChannelDAO;
import daos.UserChannelDAO;
import model.Channel;
import model.Role;

import java.sql.SQLException;

public class ChannelService {
	
	private final ChannelDAO channelDao;

	public ChannelService(ChannelDAO channelDao) {
		this.channelDao = channelDao;
	}
	
	public Channel create(String name, String profilePictureUrl) throws SQLException {
		
		return channelDao.save(name, profilePictureUrl);
	}
	
	public List<Channel> findChannelsByUser(long userId) throws SQLException {
		return channelDao.findByUser(userId);
	}

	public Channel getChannelById(long id) throws SQLException {
		return channelDao.findById(id, UserSession.getUser() != null ? UserSession.getUser().getId() : -1);
	}

    public List<Channel> searchChannels(String query) throws SQLException {
        return channelDao.findChannelsByName(query);
    }

    public void incrementSubscriberCount(long channelId) throws SQLException {
        channelDao.incrementSubscribers(channelId);
    }

    public void decrementSubscriberCount(long channelId) throws SQLException {
        channelDao.decrementSubscribers(channelId);
    }
}
