package service;

import daos.UserDAO;
import model.User;

public class UserService {
	
	UserDAO userDao;
	
	public UserService(UserDAO userDao) {
		this.userDao = userDao;
	}

	public void register(String email, String password, String username, String name) {
		
		User newUser = new User(email, password, username, name);
		
		userDao.save(newUser);
	}
	
}
