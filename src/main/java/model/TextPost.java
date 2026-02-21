package model;

public class TextPost extends Post {
    private String content;

    public TextPost() {
        this.setPostType(PostType.TEXT);
    }

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
    
    
}