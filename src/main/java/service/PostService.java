package service;


import java.sql.SQLException;

import daos.PostDAO;
import model.Post;
import model.TextPost;
import model.Video;
import model.VideoCategory;
import daos.PostDAO;
import service.CommentService;
import service.FileService;

public class PostService {

    private daos.PostDAO postDAO;
    private service.CommentService commentService;
    private service.FileService fileService;

    public PostService(daos.PostDAO postDAO, service.CommentService commentService, service.FileService fileService) {
        this.postDAO = postDAO;
        this.commentService = commentService;
        this.fileService = fileService;
    }

    public void publishPost(Post post) throws IllegalArgumentException, SQLException {
        
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("O título do post é obrigatório.");
        }
        if (post.getChannelId() == null) {
            throw new IllegalArgumentException("O post deve estar vinculado a um canal.");
        }

        if (post instanceof Video) {
            validateVideo((Video) post);
        } else if (post instanceof TextPost) {
            validateText((TextPost) post);
        }

        postDAO.save(post);
    }

    private void validateVideo(Video video) {
        if (video.getVideoUrl() == null || video.getVideoUrl().isEmpty()) {
            throw new IllegalArgumentException("O arquivo de vídeo é obrigatório.");
        }
        if (video.getDurationSeconds() == null || video.getDurationSeconds() < 0) {
            throw new IllegalArgumentException("A duração do vídeo é inválida.");
        }
        
        if (video.getDurationSeconds() <= 60) {
            video.setCategory(VideoCategory.SHORT);
        } else {
            video.setCategory(VideoCategory.LONG);
        }
    }

        private void validateText(TextPost text) {
            if (text.getContent() == null || text.getContent().length() < 10) {
                throw new IllegalArgumentException("O conteúdo do texto deve ter pelo menos 10 caracteres.");
            }
        }
    
            public java.util.List<Post> getAllPosts() throws SQLException {
        return postDAO.findAll();
    }

    public java.util.List<Post> getPostsByChannel(long channelId) throws SQLException {
                return postDAO.findAllByChannelId(channelId);
            }
        
                public Post getPostById(long id) throws SQLException {
                    return postDAO.findById(id);
                }
            
                public void deletePost(long id) throws SQLException {
                    model.Post post = postDAO.findById(id);
                    if (post != null) {
                        commentService.deleteCommentsByPost(id);

                        if (post instanceof model.Video videoPost) {
                            if (videoPost.getVideoUrl() != null && !videoPost.getVideoUrl().isEmpty()) {
                                fileService.deleteFile(videoPost.getVideoUrl());
                            }
                            if (videoPost.getThumbnailUrl() != null && !videoPost.getThumbnailUrl().isEmpty()) {
                                fileService.deleteFile(videoPost.getThumbnailUrl());
                            }
                        }
                    }
                    postDAO.delete(id);
                }

                public java.util.List<model.Post> findPostsByAuthor(long authorId) throws SQLException {
                    return postDAO.findAllByAuthorId(authorId);
                }

                public java.util.List<model.Post> findPostsByChannel(long channelId) throws SQLException {
                    return postDAO.findAllByChannelId(channelId);
                }

                public void toggleLike(long userId, long postId, boolean isLike) throws SQLException {
                    postDAO.toggleLike(userId, postId, isLike);
                }

                public void incrementViews(long postId) throws SQLException {
                    if (!UserSession.hasViewed(postId)) {
                        postDAO.incrementViews(postId);
                        UserSession.markAsViewed(postId);
                    }
                }

                public Boolean getUserReaction(long userId, long postId) throws SQLException {
                    return postDAO.getUserReaction(userId, postId);
                }
            }
            