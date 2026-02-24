package service;

import java.util.List;

import daos.ChannelDAO;
import daos.UserChannelDAO;
import model.Channel;
import model.Role;
import service.PostService;
import service.CommentService;
import service.FileService;

import java.sql.SQLException;

public class ChannelService {
	
	private final ChannelDAO channelDao;
    private PostService postService;
    private CommentService commentService;
    private FileService fileService;

	public ChannelService(ChannelDAO channelDao,
                          PostService postService,
                          CommentService commentService,
                          FileService fileService) {
		this.channelDao = channelDao;
        this.postService = postService;
        this.commentService = commentService;
        this.fileService = fileService;
	}
	
	public Channel create(String name, String profilePictureUrl) throws SQLException {
		
		return channelDao.save(name, profilePictureUrl);
	}
	
	public List<Channel> findChannelsByUser(long userId) throws SQLException {
		return channelDao.findByUser(userId);
	}

	public Channel getChannelById(long id) throws SQLException {
		return channelDao.findById(id, UserSession.getUser() != null ? UserSession.getUser().getId() : -1);
	}

    public List<Channel> searchChannels(String query) throws SQLException {
        return channelDao.findChannelsByName(query);
    }

    public void incrementSubscriberCount(long channelId) throws SQLException {
        channelDao.incrementSubscribers(channelId);
    }

    public void decrementSubscriberCount(long channelId) throws SQLException {
        channelDao.decrementSubscribers(channelId);
    }

    public void deleteChannel(long channelId) throws SQLException {
        java.util.List<model.Post> channelPosts = postService.findPostsByChannel(channelId);
        for (model.Post post : channelPosts) {
            postService.deletePost(post.getId());
        }

        channelDao.delete(channelId);
    }

    public void updateChannelName(long channelId, String newName) throws SQLException {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do canal não pode ser vazio.");
        }

        channelDao.updateName(channelId, newName);
    }

    public void updateChannelProfilePicture(long channelId, String newProfilePictureUrl) throws SQLException {
        Channel oldChannel = channelDao.findById(channelId, UserSession.getUser() != null ? UserSession.getUser().getId() : -1);
        if (oldChannel == null) {
            throw new IllegalArgumentException("Canal não encontrado para atualização da imagem de perfil.");
        }

        if (newProfilePictureUrl != null && !newProfilePictureUrl.isEmpty() &&
            oldChannel.getProfilePictureUrl() != null && !oldChannel.getProfilePictureUrl().equals(newProfilePictureUrl)) {
            fileService.deleteFile(oldChannel.getProfilePictureUrl());
        }

        channelDao.updateProfilePicture(channelId, newProfilePictureUrl);
    }
}
