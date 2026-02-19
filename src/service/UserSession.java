package service;

import model.User;
import java.util.HashSet;
import java.util.Set;

public class UserSession {

    private static User currentUser;
    private static Set<Long> viewedVideos = new HashSet<>();

    public static void login(User user) {
        currentUser = user;
        viewedVideos.clear();
        ChannelSession.close();
    }

    public static User getUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
        viewedVideos.clear();
        ChannelSession.close();
    }

    public static boolean isLogged() {
        return currentUser != null;
    }

    public static boolean hasViewed(Long postId) {
        return viewedVideos.contains(postId);
    }

    public static void markAsViewed(Long postId) {
        viewedVideos.add(postId);
    }
}
