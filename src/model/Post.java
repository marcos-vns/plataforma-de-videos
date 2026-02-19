package model;

public abstract class Post {
	
    private Long id;
    private Long channelId;
    private String title;
    private String thumbnailUrl;
    private PostType tipoPost; // Enum: VIDEO, TEXTO
    private int likes;
    private int dislikes;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getChannelId() {
		return channelId;
	}
	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public PostType getTipoPost() {
		return tipoPost;
	}
	public void setTipoPost(PostType tipoPost) {
		this.tipoPost = tipoPost;
	}
    public int getLikes() {
        return likes;
    }
    public void setLikes(int likes) {
        this.likes = likes;
    }
    public int getDislikes() {
        return dislikes;
    }
    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }
	
    @Override
    public String toString() {
        return "[" + tipoPost + "] " + title;
    }
}
