package service;

import daos.UserChannelDAO;
import daos.UserDAO;
import model.Role;
import model.User;

import java.sql.SQLException;

public class UserChannelService {
	
	UserChannelDAO userChannelDao;
	UserDAO userDao;
	ChannelService channelService;
	
	public UserChannelService(UserChannelDAO userChannelDao, UserDAO userDao, ChannelService channelService) {
		this.userChannelDao = userChannelDao;
		this.userDao = userDao;
		this.channelService = channelService;
	}
	
	public void addUserToChannelCreation(long userId, long channelId, Role role) throws SQLException {
		
		if (userChannelDao.exists(userId, channelId)) {
	        throw new RuntimeException("Usuario ja pertence ao canal");
	    }

	    userChannelDao.save(userId, channelId, role);
	}
	
	public void addUserToChannel(String username, long channelId, Role role) throws SQLException {
			
		User currentUser = UserSession.getUser();
        
        if (currentUser == null) {
            throw new RuntimeException("Usuário não autenticado");
        }
        
        boolean allowed = userChannelDao.hasPermission(
                currentUser.getId(),
                channelId,
                Role.OWNER
        );

        if (!allowed) {
            throw new RuntimeException("Sem permissao para adicionar membros");
        }
        
        User user = userDao.findByUsername(username);
		
		if(user == null) {
			throw new RuntimeException("Usuario informado nao existe");
		}
        
        if (userChannelDao.exists(user.getId(), channelId)) {
	        throw new RuntimeException("Usuario ja pertence ao canal");
	    }

	    userChannelDao.save(user.getId(), channelId, role);
	}

    public boolean isUserSubscribed(long userId, long channelId) throws SQLException {
        return userChannelDao.isUserSubscribed(userId, channelId);
    }

    public void subscribe(long userId, long channelId) throws SQLException {
        userChannelDao.save(userId, channelId, Role.SUBSCRIBER);
        channelService.incrementSubscriberCount(channelId);
    }

    public void unsubscribe(long userId, long channelId) throws SQLException {
        userChannelDao.removeUserChannel(userId, channelId);
        channelService.decrementSubscriberCount(channelId);
    }

    public Role getRole(long userId, long channelId) throws SQLException {
        return userChannelDao.getRole(userId, channelId);
    }
}
