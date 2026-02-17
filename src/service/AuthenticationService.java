package service;

import daos.UserDAO;
import model.User;

public class AuthenticationService {

    private final UserDAO userDAO;

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean login(String email, String password) {

        User user = userDAO.findByEmail(email);

        if (user == null) {
            return false;
        }

        boolean valid =
            PasswordService.matches(password, user.getPasswordHash());

        if (!valid) {
            return false;
        }

        Session.login(user);
        
        System.out.println("logado!");
        
        return true;
    }

    public void logout() {
        Session.logout();
    }
}

