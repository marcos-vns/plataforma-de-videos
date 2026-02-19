package model;

import java.sql.Timestamp;

public class Comment {
    private long id;
    private long postId;
    private long userId;
    private String username; // Auxiliary for UI
    private String text;
    private Timestamp createdAt;

    public Comment() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getPostId() { return postId; }
    public void setPostId(long postId) { this.postId = postId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return (username != null ? username : "Usuário") + ": " + text;
    }
}
