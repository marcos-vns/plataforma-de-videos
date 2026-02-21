package service;

import java.util.List;

import daos.ChannelDAO;
import daos.UserChannelDAO;
import model.Channel;
import model.Role;

public class ChannelService {
	
	private final ChannelDAO channelDao;
	private final UserChannelService userChannelService;
	
	public ChannelService(ChannelDAO channelDao, UserChannelService userChannelService) {
		this.channelDao = channelDao;
		this.userChannelService = userChannelService;
	}
	
	public void create(String name, String profilePictureUrl) {
		
		Channel newChannel = channelDao.save(name, profilePictureUrl);
		userChannelService.addUserToChannelCreation(UserSession.getUser().getId(),
				newChannel.getId(),
				Role.OWNER
			);
	}
	
	public List<Channel> findChannelsByUser(long userId) {
		return channelDao.findByUser(userId);
	}

	public Channel getChannelById(long id) {
		return channelDao.findById(id, UserSession.getUser() != null ? UserSession.getUser().getId() : -1);
	}
	
}
