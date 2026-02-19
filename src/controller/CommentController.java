package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import service.CommentService;

public class CommentController {

    private CommentService commentService;
    private long postId;

    @FXML
    private TextField commentField;

    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }

    @FXML
    private void sendComment() {
        try {
            commentService.createComment(
                postId,
                commentField.getText()
            );
            commentField.clear();
        } catch (Exception e) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setContentText("Erro ao enviar comentário: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
