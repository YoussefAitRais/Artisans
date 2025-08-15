package org.event.backend.service.media;

import org.event.backend.dto.media.MediaResponse;
import org.event.backend.dto.media.MediaUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Media;
import org.event.backend.entity.MediaStatus;
import org.event.backend.repository.MediaRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    // simple local folder for MVP; can be moved to S3 later
    private final Path uploadRoot = Paths.get("uploads");

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    // ---------- Public listing ----------
    @Transactional(readOnly = true)
    public List<MediaResponse> publicPortfolio(Long artisanId) {
        return mediaRepository
                .findByArtisan_IdAndIsPublicTrueAndStatusOrderByCreatedAtDesc(
                        artisanId, MediaStatus.APPROVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- Owner listing ----------
    @Transactional(readOnly = true)
    public List<MediaResponse> myPortfolio(Artisan artisan) {
        return mediaRepository
                .findByArtisan_IdOrderByCreatedAtDesc(artisan.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- Upload ----------
    @Transactional
    public MediaResponse upload(Artisan artisan,
                                MultipartFile file,
                                String title,
                                String description,
                                String tags,
                                Boolean isPublic) {
        validateImage(file);

        try {
            Files.createDirectories(uploadRoot);

            String ext = getExtension(file.getOriginalFilename());
            String stored = UUID.randomUUID().toString().replace("-", "") + ext;
            Path dest = uploadRoot.resolve(stored).normalize();
            file.transferTo(dest.toFile());

            Media m = new Media();
            m.setArtisan(artisan);
            m.setTitle(safeTrim(title));
            m.setDescription(safeTrim(description));
            m.setTags(safeTrim(tags));
            m.setStorageKey(stored);
            m.setUrl(null); // will be set after id exists
            m.setThumbUrl(null);
            m.setMediumUrl(null);
            m.setPublic(isPublic == null || isPublic);
            m.setStatus(MediaStatus.APPROVED); // no moderation in MVP
            m.setCover(false);
            m.setSizeBytes(file.getSize());
            m.setContentType(file.getContentType());
            m.setCreatedAt(Instant.now());
            m.setUpdatedAt(Instant.now());

            // persist to get ID
            m = mediaRepository.save(m);

            // Streaming link served by controller
            String fileUrl = "/api/media/" + m.getId() + "/file";
            m.setUrl(fileUrl);
            m = mediaRepository.save(m);

            return toResponse(m);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store file: " + e.getMessage(), e);
        }
    }

    // ---------- Update metadata ----------
    @Transactional
    public MediaResponse updateMetadata(Artisan artisan, Long id, MediaUpdateRequest req) {
        Media m = mediaRepository.findByIdAndArtisan_Id(id, artisan.getId())
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        if (req.getTitle() != null) m.setTitle(safeTrim(req.getTitle()));
        if (req.getDescription() != null) m.setDescription(safeTrim(req.getDescription()));
        if (req.getTags() != null) m.setTags(safeTrim(req.getTags()));
        if (req.getIsPublic() != null) m.setPublic(req.getIsPublic());

        return toResponse(m);
    }

    // ---------- Set cover ----------
    @Transactional
    public void setCover(Artisan artisan, Long id) {
        Media m = mediaRepository.findByIdAndArtisan_Id(id, artisan.getId())
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        mediaRepository.clearCoverForArtisan(artisan.getId());
        m.setCover(true);
    }

    // ---------- Delete ----------
    @Transactional
    public void delete(Artisan artisan, Long id) {
        Media m = mediaRepository.findByIdAndArtisan_Id(id, artisan.getId())
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        // delete file from disk
        try {
            Path file = uploadRoot.resolve(m.getStorageKey()).normalize();
            Files.deleteIfExists(file);
        } catch (Exception ignored) {}

        mediaRepository.delete(m);
    }

    // ---------- File streaming ----------
    @Transactional(readOnly = true)
    public Resource loadAsResource(Long mediaId) {
        Media m = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        Path file = uploadRoot.resolve(m.getStorageKey()).normalize();
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                throw new IllegalArgumentException("File not found on disk");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid file path", e);
        }
    }

    // ---------- Helpers ----------
    private MediaResponse toResponse(Media m) {
        return new MediaResponse(
                m.getId(),
                m.getArtisan().getId(),
                m.getTitle(),
                m.getDescription(),
                m.getTags(),
                m.getUrl(),
                m.getThumbUrl(),
                m.getMediumUrl(),
                m.isPublic(),
                m.isCover(),
                m.getStatus(),
                m.getWidth(),
                m.getHeight(),
                m.getSizeBytes(),
                m.getContentType(),
                m.getCreatedAt()
        );
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        String ct = file.getContentType();
        if (ct == null || !(ct.startsWith(MediaType.IMAGE_JPEG_VALUE)
                || ct.startsWith(MediaType.IMAGE_PNG_VALUE)
                || ct.startsWith("image/webp"))) {
            throw new IllegalArgumentException("Only JPEG/PNG/WEBP images are allowed");
        }
        // Optional: size limit guard, e.g., 5MB
        long max = 5L * 1024 * 1024;
        if (file.getSize() > max) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }
    }

    private String getExtension(String original) {
        String name = (original == null) ? "" : original;
        String ext = StringUtils.getFilenameExtension(name);
        return (ext == null || ext.isBlank()) ? "" : "." + ext.toLowerCase();
    }

    private String safeTrim(String v) {
        return (v == null) ? null : v.trim();
    }
}
