package service;

import java.sql.SQLException;
import java.util.List;
import daos.CommentDAO;
import model.Comment;
import model.User;
import daos.CommentDAO;

public class CommentService {
    private daos.CommentDAO commentDAO;

    public CommentService(daos.CommentDAO commentDAO) {
        this.commentDAO = commentDAO;
    }

    public void createComment(long postId, String text) throws SQLException {
        User user = UserSession.getUser();
        if (user == null) {
            throw new RuntimeException("Usuário deve estar logado para comentar.");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("O comentário não pode estar vazio.");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setText(text);
        
        commentDAO.save(comment);
    }

    public void createReply(long postId, long parentId, String text) throws SQLException {
        User user = UserSession.getUser();
        if (user == null) {
            throw new RuntimeException("Usuário deve estar logado para responder.");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("A resposta não pode estar vazia.");
        }

        Comment reply = new Comment();
        reply.setPostId(postId);
        reply.setUserId(user.getId());
        reply.setText(text);
        reply.setParentId(parentId);
        
        commentDAO.save(reply);
    }

    public List<Comment> getCommentsByPost(long postId) throws SQLException {
        return commentDAO.findByPostId(postId);
    }

    public void deleteComment(long commentId) throws java.sql.SQLException {
        commentDAO.delete(commentId);
    }

    public void deleteCommentsByUser(long userId) throws java.sql.SQLException {
        commentDAO.deleteByUserId(userId); // This method needs to be implemented in CommentDAO
    }

    public void deleteCommentsByPost(long postId) throws java.sql.SQLException {
        commentDAO.deleteByPostId(postId); // This method needs to be implemented in CommentDAO
    }
}
