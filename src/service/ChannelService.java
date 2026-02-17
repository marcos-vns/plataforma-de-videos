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
	
	public void create(String name) {
		
		Channel newChannel = channelDao.save(name);
		userChannelService.addUserToChannelCreation(Session.getUser().getId(),
				newChannel.getId(),
				Role.OWNER
			);
	}
	
	public List<Channel> findChannelsByUser(long userId) {
		return channelDao.findByUser(userId);
	}
	
}
