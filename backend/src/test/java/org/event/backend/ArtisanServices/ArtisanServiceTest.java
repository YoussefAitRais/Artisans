package org.event.backend.ArtisanServices;

import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.artisan.ArtisanUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Utilisateur;
import org.event.backend.repository.ArtisanRepository;
import org.event.backend.service.artisan.ArtisanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Minimal, high-value unit tests with clear names and simple logic.
 */
@ExtendWith(MockitoExtension.class)
class ArtisanServiceTest {

    @Mock private ArtisanRepository artisanRepository;
    @InjectMocks private ArtisanService artisanService;

    @Captor private ArgumentCaptor<Artisan> artisanCaptor;

    private Artisan sample;

    @BeforeEach
    void setUp() {
        // Seed sample entity once for reuse
        sample = new Artisan();
        sample.setId(1L);
        sample.setNom("Youssef");
        sample.setPrenom("Ait");
        sample.setEmail("youssef@example.com");
        sample.setMetier("Electricien");
        sample.setLocalisation("Fes");
        sample.setDescription("Pro artisan");
    }

    // ---------- SEARCH ----------

    @Test
    void searchArtisans_mapsEntitiesToDtos() {
        Pageable page = PageRequest.of(0, 10);
        when(artisanRepository.findAll(any(Specification.class), eq(page)))
                .thenReturn(new PageImpl<>(List.of(sample), page, 1));

        Page<ArtisanResponse> result = artisanService.searchArtisans("elec", "fes", "yous", page);

        assertEquals(1, result.getTotalElements(), "Should return one result");
        ArtisanResponse dto = result.getContent().get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Youssef", dto.getNom());
        verify(artisanRepository).findAll(any(Specification.class), eq(page));
    }

    // ---------- GET BY ID ----------

    @Test
    void getArtisanById_found_returnsDto() {
        when(artisanRepository.findById(1L)).thenReturn(Optional.of(sample));

        ArtisanResponse dto = artisanService.getArtisanById(1L);

        assertEquals("Youssef", dto.getNom());
        verify(artisanRepository).findById(1L);
    }

    @Test
    void getArtisanById_notFound_throwsIllegalArgument() {
        when(artisanRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> artisanService.getArtisanById(99L)
        );
        assertTrue(ex.getMessage().contains("Artisan not found"));
    }

    // ---------- SELF PROFILE ----------

    @Test
    void getCurrentArtisanProfile_usesCurrentUserId() {
        Utilisateur current = mock(Utilisateur.class);
        when(current.getId()).thenReturn(1L);
        when(artisanRepository.findById(1L)).thenReturn(Optional.of(sample));

        ArtisanResponse dto = artisanService.getCurrentArtisanProfile(current);

        assertEquals(1L, dto.getId());
        verify(artisanRepository).findById(1L);
    }

    @Test
    void updateCurrentArtisanProfile_trimsInput_andSaves() {
        // Arrange current user and existing artisan
        Utilisateur current = mock(Utilisateur.class);
        when(current.getId()).thenReturn(1L);
        when(artisanRepository.findById(1L)).thenReturn(Optional.of(sample));
        when(artisanRepository.save(any(Artisan.class))).thenAnswer(i -> i.getArgument(0));

        // Use a mocked DTO to keep logic simple and independent of constructors
        ArtisanUpdateRequest req = mock(ArtisanUpdateRequest.class);
        when(req.getNom()).thenReturn("  John  ");
        when(req.getPrenom()).thenReturn("  Doe ");
        when(req.getMetier()).thenReturn("  Plombier ");
        when(req.getLocalisation()).thenReturn("  Rabat  ");
        when(req.getDescription()).thenReturn("  Best in town  ");

        // Act
        ArtisanResponse dto = artisanService.updateCurrentArtisanProfile(current, req);

        // Assert saved entity (trimmed) and returned DTO
        verify(artisanRepository).save(artisanCaptor.capture());
        Artisan saved = artisanCaptor.getValue();
        assertEquals("John", saved.getNom());
        assertEquals("Doe", saved.getPrenom());
        assertEquals("Plombier", saved.getMetier());
        assertEquals("Rabat", saved.getLocalisation());
        assertEquals("Best in town", saved.getDescription());

        assertEquals("John", dto.getNom());
        assertEquals("Doe", dto.getPrenom());
    }

    // ---------- ADMIN DELETE ----------

    @Test
    void deleteArtisanAsAdmin_existing_deletes() {
        when(artisanRepository.existsById(1L)).thenReturn(true);

        artisanService.deleteArtisanAsAdmin(1L);

        verify(artisanRepository).deleteById(1L);
    }

    @Test
    void deleteArtisanAsAdmin_notExisting_throwsIllegalArgument() {
        when(artisanRepository.existsById(999L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> artisanService.deleteArtisanAsAdmin(999L)
        );
        assertTrue(ex.getMessage().contains("Artisan not found"));
        verify(artisanRepository, never()).deleteById(anyLong());
    }
}
