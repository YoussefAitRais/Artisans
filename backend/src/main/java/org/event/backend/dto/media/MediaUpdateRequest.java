package org.event.backend.dto.media;

import jakarta.validation.constraints.Size;

/** Update metadata of a media item (no binary file here). */
public class MediaUpdateRequest {

    @Size(max = 150)
    private String title;

    @Size(max = 500)
    private String description;

    @Size(max = 250)
    private String tags;

    private Boolean isPublic;   // nullable => keep unchanged

    // Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean aPublic) { isPublic = aPublic; }
}
