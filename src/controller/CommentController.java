package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import service.CommentService;

public class CommentController {

    private CommentService commentService;

    @FXML
    private TextField commentField;

    private long postId;

    @FXML
    private void sendComment() {

        commentService.createComment(
            postId,
            commentField.getText(),
            null
        );
    }
}
