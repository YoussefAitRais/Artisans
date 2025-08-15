package org.event.backend.dto.media;

import org.event.backend.entity.MediaStatus;

import java.time.Instant;

/** What the frontend needs to render an image card in the portfolio grid. */
public class MediaResponse {

    private Long id;
    private Long artisanId;

    private String title;
    private String description;
    private String tags;

    /** Public URL to stream the file (served by controller) */
    private String url;

    private String thumbUrl;   // optional (null for MVP)
    private String mediumUrl;  // optional (null for MVP)

    private boolean isPublic;
    private boolean isCover;
    private MediaStatus status;

    private Integer width;
    private Integer height;
    private Long sizeBytes;
    private String contentType;

    private Instant createdAt;

    public MediaResponse() {}

    public MediaResponse(Long id, Long artisanId, String title, String description, String tags,
                         String url, String thumbUrl, String mediumUrl,
                         boolean isPublic, boolean isCover, MediaStatus status,
                         Integer width, Integer height, Long sizeBytes, String contentType,
                         Instant createdAt) {
        this.id = id;
        this.artisanId = artisanId;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.url = url;
        this.thumbUrl = thumbUrl;
        this.mediumUrl = mediumUrl;
        this.isPublic = isPublic;
        this.isCover = isCover;
        this.status = status;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getArtisanId() { return artisanId; }
    public void setArtisanId(Long artisanId) { this.artisanId = artisanId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }

    public String getMediumUrl() { return mediumUrl; }
    public void setMediumUrl(String mediumUrl) { this.mediumUrl = mediumUrl; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public boolean isCover() { return isCover; }
    public void setCover(boolean cover) { isCover = cover; }

    public MediaStatus getStatus() { return status; }
    public void setStatus(MediaStatus status) { this.status = status; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
