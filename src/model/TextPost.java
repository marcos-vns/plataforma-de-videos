package model;

public class TextPost extends Post {
    private String content;

    public TextPost() {
        this.setTipoPost(PostType.TEXTO);
    }

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
    
    
}