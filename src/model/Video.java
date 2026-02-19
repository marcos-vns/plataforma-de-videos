package model;

public class Video extends Post {
	
    private String description;
    private Integer durationSeconds;
    private long views;
    private String videoUrl;
    private VideoCategory category; // Enum: LONGO, SHORT
    
    public Video() {
        this.setPostType(PostType.VIDEO);
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

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
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