package service;

import daos.ChannelDAO;
import daos.UserDAO;
import model.User;
import service.ChannelService;
import service.PostService;
import service.CommentService;
import service.WatchHistoryService;
import service.FileService;

public class UserService {
	
	UserDAO userDao;
    private ChannelService channelService;
    private PostService postService;
    private CommentService commentService;
    private WatchHistoryService watchHistoryService;
    private FileService fileService;
	
	public UserService(UserDAO userDao,
                       ChannelService channelService,
                       PostService postService,
                       CommentService commentService,
                       WatchHistoryService watchHistoryService,
                       FileService fileService) {
		this.userDao = userDao;
        this.channelService = channelService;
        this.postService = postService;
        this.commentService = commentService;
        this.watchHistoryService = watchHistoryService;
        this.fileService = fileService;
	}

	public void register(String email, String password, String username, String name, String profilePictureUrl) {
		
		if (userDao.findByEmail(email) != null) {
	        throw new RuntimeException("Email ja cadastrado");
	    }

	    if (userDao.findByUsername(username) != null) {
	        throw new RuntimeException("Username ja existe");
	    }

	    String hash = PasswordService.hashPassword(password);
		
		User newUser = new User(email, hash, username, name);
		newUser.setProfilePictureUrl(profilePictureUrl);
		userDao.save(newUser);
	}

    public void deleteUser(long userId) throws java.sql.SQLException {
        try {
            java.util.List<model.Channel> userChannels = channelService.findChannelsByUser(userId);
            for (model.Channel channel : userChannels) {
                channelService.deleteChannel(channel.getId());
            }
        } catch (Exception e) {
            System.err.println("Error deleting user's channels: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to indicate failure
        }


        try {
            commentService.deleteCommentsByUser(userId);
        } catch (Exception e) {
            System.err.println("Error deleting user's comments: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        try {
            watchHistoryService.deleteHistoryByUser(userId);
        } catch (Exception e) {
            System.err.println("Error deleting user's watch history: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        try {
            model.User userToDelete = userDao.findById(userId);
            if (userToDelete != null && userToDelete.getProfilePictureUrl() != null && !userToDelete.getProfilePictureUrl().isEmpty()) {
                fileService.deleteFile(userToDelete.getProfilePictureUrl());
            }
        } catch (Exception e) {
            System.err.println("Error deleting user's profile picture: " + e.getMessage());
            e.printStackTrace();
        }


        userDao.delete(userId);
    }
	
}
