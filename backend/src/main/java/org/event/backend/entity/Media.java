package org.event.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Portfolio media item uploaded by an Artisan.
 * Stores public/derived URLs and basic technical metadata.
 */
@Entity
@Table(
        name = "media",
        indexes = {
                @Index(name = "idx_media_artisan", columnList = "artisan_id"),
                @Index(name = "idx_media_status", columnList = "status"),
                @Index(name = "idx_media_public", columnList = "is_public")
        }
)
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner artisan of this media item */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "artisan_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_media_artisan")
    )
    private Artisan artisan;

    /** Optional human title */
    @Column(length = 150)
    private String title;

    /** Optional description (short) */
    @Column(length = 500)
    private String description;

    /** Optional tags, comma separated: e.g. "kitchen,wood,modern" */
    @Column(length = 250)
    private String tags;

    /** Public URL (CDN or static server) used by the frontend */
    @Column(nullable = false, length = 500)
    private String url;

    /** Storage key/path (S3 key or local filesystem path) */
    @Column(nullable = false, length = 500)
    private String storageKey;

    /** Derived variant URLs for performance */
    @Column(length = 500)
    private String thumbUrl;    // small thumbnail (e.g., 400px)

    @Column(length = 500)
    private String mediumUrl;   // medium size (e.g., 1200px)

    /** Visibility flag (true by default) */
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    /** Moderation status (APPROVED by default for MVP) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaStatus status = MediaStatus.APPROVED;

    /** Mark as profile cover image (enforce single cover per artisan at service level) */
    @Column(name = "is_cover", nullable = false)
    private boolean isCover = false;

    /** Technical metadata (optional) */
    private Integer width;       // px
    private Integer height;      // px

    @Column(name = "size_bytes")
    private Long sizeBytes;      // file size

    @Column(length = 100)
    private String contentType;  // image/jpeg | image/png | image/webp

    /** Audit columns */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = MediaStatus.APPROVED; // default for MVP
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // ----- Constructors -----
    public Media() {}

    // ----- Getters / Setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Artisan getArtisan() { return artisan; }
    public void setArtisan(Artisan artisan) { this.artisan = artisan; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }

    public String getMediumUrl() { return mediumUrl; }
    public void setMediumUrl(String mediumUrl) { this.mediumUrl = mediumUrl; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean aPublic) { isPublic = aPublic; }

    public MediaStatus getStatus() { return status; }
    public void setStatus(MediaStatus status) { this.status = status; }

    public boolean isCover() { return isCover; }
    public void setCover(boolean cover) { isCover = cover; }

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

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
