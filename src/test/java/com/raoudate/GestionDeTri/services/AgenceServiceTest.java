package com.raoudate.GestionDeTri.services;
import com.raoudate.GestionDeTri.dto.response.AgenceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgenceServiceTest {

    @Mock
    private AgenceService agenceService;

    private AgenceDTO testAgence;

    @BeforeEach
    void setUp() {
        testAgence = AgenceDTO.builder()
                .id(1)
                .code("AG001")
                .nom("Agence Test")
                .numTel("+228 12345678")
                .adresseComplete("123 Rue Test")
                .build();
    }

    @Test
    void testSaveAgence_Success() {
        when(agenceService.save(any(AgenceDTO.class))).thenReturn(testAgence);

        AgenceDTO result = agenceService.save(testAgence);

        assertNotNull(result);
        assertEquals("AG001", result.getCode());
        assertEquals("Agence Test", result.getNom());
        verify(agenceService, times(1)).save(testAgence);
    }

    @Test
    void testFindAllAgences() {
        List<AgenceDTO> agences = Arrays.asList(testAgence);
        when(agenceService.findAll()).thenReturn(agences);

        List<AgenceDTO> result = agenceService.findAll();

        assertEquals(1, result.size());
        verify(agenceService, times(1)).findAll();
    }

    @Test
    void testDeleteAgence() {
        doNothing().when(agenceService).delete(1);

        agenceService.delete(1);

        verify(agenceService, times(1)).delete(1);
    }
}