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
        
        Role oldRole = userChannelDao.getRole(user.getId(), channelId);

        if (oldRole == null) {
            // User not in channel, save new entry
            userChannelDao.save(user.getId(), channelId, role);
            if (role == Role.SUBSCRIBER) {
                channelService.incrementSubscriberCount(channelId);
            }
        } else if (oldRole != role) {
            // User is in channel with a different role, update it
            userChannelDao.updateRole(user.getId(), channelId, role);
            // Adjust subscriber counts
            if (oldRole == Role.SUBSCRIBER && role != Role.SUBSCRIBER) {
                channelService.decrementSubscriberCount(channelId);
            } else if (oldRole != Role.SUBSCRIBER && role == Role.SUBSCRIBER) {
                channelService.incrementSubscriberCount(channelId);
            }
        }
        // If oldRole == role, do nothing (user already has this role)
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
