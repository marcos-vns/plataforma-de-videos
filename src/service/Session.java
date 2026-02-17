package service;

import model.User;

public class Session {

    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLogged() {
        return currentUser != null;
    }
}
