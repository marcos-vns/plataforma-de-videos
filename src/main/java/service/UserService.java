package service;

import daos.ChannelDAO;
import daos.UserDAO;
import model.User;
import service.ChannelService;
import service.PostService;
import service.CommentService;
import service.WatchHistoryService;
import service.FileService;
import daos.UserChannelDAO; // Add this import

public class UserService {
	
	UserDAO userDao;
    private ChannelService channelService;
    private PostService postService;
    private CommentService commentService;
    private WatchHistoryService watchHistoryService;
    private FileService fileService;
    private UserChannelDAO userChannelDAO;
	
	public UserService(UserDAO userDao,
                       ChannelService channelService,
                       PostService postService,
                       CommentService commentService,
                       WatchHistoryService watchHistoryService,
                       FileService fileService,
                       UserChannelDAO userChannelDAO) {
		this.userDao = userDao;
        this.channelService = channelService;
        this.postService = postService;
        this.commentService = commentService;
        this.watchHistoryService = watchHistoryService;
        this.fileService = fileService;
        this.userChannelDAO = userChannelDAO;
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
            java.util.List<model.Channel> userAssociatedChannels = channelService.findChannelsByUser(userId);
            for (model.Channel channel : userAssociatedChannels) {
                model.Role userRole = userChannelDAO.getRole(userId, channel.getId());
                if (userRole == model.Role.OWNER) {
                    int ownerCount = userChannelDAO.countOwnersForChannel(channel.getId());
                    if (ownerCount == 1) {
                        channelService.deleteChannel(channel.getId());
                    } else {
                        userChannelDAO.removeUserChannel(userId, channel.getId());
                    }
                } else {
                    userChannelDAO.removeUserChannel(userId, channel.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar canais associados ao usuário: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }



        try {
            commentService.deleteCommentsByUser(userId);
        } catch (Exception e) {
            System.err.println("Erro ao deletar comentarios: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        try {
            watchHistoryService.deleteHistoryByUser(userId);
        } catch (Exception e) {
            System.err.println("Erro ao deletar historico: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        try {
            model.User userToDelete = userDao.findById(userId);
            if (userToDelete != null && userToDelete.getProfilePictureUrl() != null && !userToDelete.getProfilePictureUrl().isEmpty()) {
                fileService.deleteFile(userToDelete.getProfilePictureUrl());
            }
        } catch (Exception e) {
            System.err.println("Error ao deletar foto de perfil: " + e.getMessage());
            e.printStackTrace();
        }


        userDao.delete(userId);
    }
	
}
