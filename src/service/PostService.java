package service;


import java.sql.SQLException;

import daos.PostDAO;
import model.Post;
import model.TextPost;
import model.Video;
import model.VideoCategory;

public class PostService {

    private PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    // Método genérico que aceita qualquer filho de Post
    public void publishPost(Post post) throws IllegalArgumentException, SQLException {
        
        // 1. Validações Comuns (Generalização)
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("O título do post é obrigatório.");
        }
        if (post.getChannelId() == null) {
            throw new IllegalArgumentException("O post deve estar vinculado a um canal.");
        }

        // 2. Validações Específicas (Especialização)
        if (post instanceof Video) {
            validateVideo((Video) post);
        } else if (post instanceof TextPost) {
            validateText((TextPost) post);
        }

        // 3. Chama o DAO para persistir
        postDAO.save(post);
    }

    private void validateVideo(Video video) {
        if (video.getVideoUrl() == null || video.getVideoUrl().isEmpty()) {
            throw new IllegalArgumentException("O arquivo de vídeo é obrigatório.");
        }
        if (video.getDurationSeconds() == null || video.getDurationSeconds() < 0) {
            throw new IllegalArgumentException("A duração do vídeo é inválida.");
        }
        
        // Regra de negócio: Definir automaticamente se é Short ou Longo
        // Exemplo: Menos de 60 segundos é Short
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
                    postDAO.delete(id);
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
            