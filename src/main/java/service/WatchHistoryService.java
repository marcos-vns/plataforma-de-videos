package service;

import daos.WatchHistoryDAO;
import model.Post;

import java.util.List;

public class WatchHistoryService {

    private final WatchHistoryDAO watchHistoryDAO;

    public WatchHistoryService(WatchHistoryDAO watchHistoryDAO) {
        this.watchHistoryDAO = watchHistoryDAO;
    }

    public void addPostToHistory(long userId, long postId) {
        watchHistoryDAO.addPostToHistory(userId, postId);
    }

    public java.util.List<model.Post> getWatchHistoryByUser(long userId) {
        return watchHistoryDAO.getWatchHistoryByUser(userId);
    }

    public void deleteHistoryByUser(long userId) {
        watchHistoryDAO.deleteHistoryByUser(userId);
    }
}
