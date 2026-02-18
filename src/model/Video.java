package model;

public class Video extends Post {
	
    private String description;
    private Integer durationSeconds;
    private String videoUrl;
    private VideoCategory category; // Enum: LONGO, SHORT
    
    public Video() {
        this.setTipoPost(PostType.VIDEO);
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getDurationSeconds() {
		return durationSeconds;
	}

	public void setDurationSeconds(Integer durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public VideoCategory getCategory() {
		return category;
	}

	public void setCategory(VideoCategory category) {
		this.category = category;
	}

    
    
    
}