package service;

import model.User;

public class UserSession {

    private static User currentUser;

    public static void login(User user) {
        currentUser = user;
        ChannelSession.close();
    }

    public static User getUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        ChannelSession.close();
    }

    public static boolean isLogged() {
        return currentUser != null;
    }
}
