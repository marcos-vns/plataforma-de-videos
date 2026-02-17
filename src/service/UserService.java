package service;

import daos.ChannelDAO;
import daos.UserDAO;
import model.User;

public class UserService {
	
	UserDAO userDao;
	
	public UserService(UserDAO userDao) {
		this.userDao = userDao;
	}

	public void register(String email, String password, String username, String name) {
		
		if (userDao.findByEmail(email) != null) {
	        throw new RuntimeException("Email ja cadastrado");
	    }

	    if (userDao.findByUsername(username) != null) {
	        throw new RuntimeException("Username ja existe");
	    }

	    String hash = PasswordService.hashPassword(password);
		
		User newUser = new User(email, hash, username, name);
		userDao.save(newUser);
	}
	
}
