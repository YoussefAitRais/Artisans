package org.event.backend.ClientServices;


import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.client.ClientResponse;
import org.event.backend.dto.client.ClientUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.Utilisateur;
import org.event.backend.repository.ArtisanRepository;
import org.event.backend.repository.ClientRepository;
import org.event.backend.repository.QuoteRepository;
import org.event.backend.service.client.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private ArtisanRepository artisanRepository; // not used in these tests, but required by ctor
    @Mock private QuoteRepository quoteRepository;     // not used directly; service uses JPQL via EntityManager
    @Mock private EntityManager entityManager;

    @InjectMocks private ClientService clientService;

    @Captor private ArgumentCaptor<Client> clientCaptor;

    private Client sampleClient;
    private Artisan sampleArtisan;

    @BeforeEach
    void setUp() {
        sampleClient = new Client();
        sampleClient.setId(1L);
        sampleClient.setNom("Youssef");
        sampleClient.setPrenom("Ait");
        sampleClient.setEmail("youssef@example.com");
        sampleClient.setTelephone("0600000000");

        sampleArtisan = new Artisan();
        sampleArtisan.setId(7L);
        sampleArtisan.setNom("Ali");
        sampleArtisan.setPrenom("Karim");
        sampleArtisan.setEmail("ali@example.com");
        sampleArtisan.setMetier("Plombier");
        sampleArtisan.setLocalisation("Rabat");
        sampleArtisan.setDescription("Expert");
    }

    // ---------- getMe ----------

    @Test
    void getMe_found_returnsDto() {
        // Arrange
        Utilisateur current = mock(Utilisateur.class);
        when(current.getId()).thenReturn(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));

        // Act
        ClientResponse dto = clientService.getMe(current);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("youssef@example.com", dto.getEmail());
        verify(clientRepository).findById(1L);
    }

    @Test
    void getMe_notFound_throws() {
        Utilisateur current = mock(Utilisateur.class);
        when(current.getId()).thenReturn(2L);
        when(clientRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> clientService.getMe(current));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    // ---------- updateMe ----------

    @Test
    void updateMe_trimsAndSaves_thenReturnsMappedDto() {
        // Arrange
        Utilisateur current = mock(Utilisateur.class);
        when(current.getId()).thenReturn(1L);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(sampleClient));
        when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

        ClientUpdateRequest req = mock(ClientUpdateRequest.class);
        when(req.getNom()).thenReturn("  John  ");
        when(req.getPrenom()).thenReturn(" Doe ");
        when(req.getTelephone()).thenReturn(" 0612345678 ");

        // Act
        ClientResponse dto = clientService.updateMe(current, req);

        // Assert saved entity trimmed
        verify(clientRepository).save(clientCaptor.capture());
        Client saved = clientCaptor.getValue();
        assertEquals("John", saved.getNom());
        assertEquals(" Doe ", saved.getPrenom(), "Only 'nom' is trimmed in service; others kept as-is");
        assertEquals(" 0612345678 ", saved.getTelephone());

        // Assert returned DTO reflects saved entity
        assertEquals("John", dto.getNom());
        assertEquals(" Doe ", dto.getPrenom());
        assertEquals(" 0612345678 ", dto.getTelephone());
    }

    // ---------- getContactedArtisans (JPQL + pagination) ----------

    @Test
    void getContactedArtisans_returnsPagedMappedArtisans() {
        // Arrange
        Utilisateur current = mock(Utilisateur.class);
        when(current.getId()).thenReturn(1L);

        Pageable page = PageRequest.of(0, 5);

        @SuppressWarnings("unchecked")
        TypedQuery<Artisan> queryArtisan = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> queryCount = mock(TypedQuery.class);

        // Route typed createQuery calls by class
        when(entityManager.createQuery(anyString(), eq(Artisan.class))).thenReturn(queryArtisan);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(queryCount);

        // Configure count
        when(queryCount.setParameter(eq("clientId"), eq(1L))).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(1L);

        // Configure list query with pagination
        when(queryArtisan.setParameter(eq("clientId"), eq(1L))).thenReturn(queryArtisan);
        when(queryArtisan.setFirstResult(anyInt())).thenReturn(queryArtisan);
        when(queryArtisan.setMaxResults(anyInt())).thenReturn(queryArtisan);
        when(queryArtisan.getResultList()).thenReturn(List.of(sampleArtisan));

        // Act
        Page<ArtisanResponse> result = clientService.getContactedArtisans(current, page);

        // Assert
        assertEquals(1, result.getTotalElements());
        ArtisanResponse first = result.getContent().get(0);
        assertEquals(7L, first.getId());
        assertEquals("Plombier", first.getMetier());
    }

    // ---------- adminList ----------

    @Test
    void adminList_blankQuery_returnsAllPaged() {
        Pageable page = PageRequest.of(0, 10);
        when(clientRepository.findAll(page)).thenReturn(new PageImpl<>(List.of(sampleClient), page, 1));

        Page<ClientResponse> result = clientService.adminList(null, page);

        assertEquals(1, result.getTotalElements());
        assertEquals("Youssef", result.getContent().get(0).getNom());
        verify(clientRepository).findAll(page);
    }

    // ---------- adminDelete ----------

    @Test
    void adminDelete_notExisting_throws() {
        when(clientRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> clientService.adminDelete(99L));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(clientRepository, never()).deleteById(anyLong());
    }

    // ---------- getTotalClientsCount ----------

    @Test
    void getTotalClientsCount_returnsRepoCount() {
        when(clientRepository.count()).thenReturn(42L);

        long count = clientService.getTotalClientsCount();

        assertEquals(42L, count);
        verify(clientRepository).count();
    }
}
