package service;

import daos.UserChannelDAO;
import daos.UserDAO;
import model.Role;
import model.User;

public class UserChannelService {
	
	UserChannelDAO userChannelDao;
	UserDAO userDao;
	
	public UserChannelService(UserChannelDAO userChannelDao, UserDAO userDao) {
		this.userChannelDao = userChannelDao;
		this.userDao = userDao;
	}
	
	public void addUserToChannelCreation(long userId, long channelId, Role role) {
		
		if (userChannelDao.exists(userId, channelId)) {
	        throw new RuntimeException("Usuario ja pertence ao canal");
	    }

	    userChannelDao.save(userId, channelId, role);
	}
	
	public void addUserToChannel(String username, long channelId, Role role) {
			
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
	
}
