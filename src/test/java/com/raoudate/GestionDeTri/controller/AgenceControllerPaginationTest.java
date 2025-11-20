package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.AgenceDTO;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour la pagination des agences
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AgenceControllerPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgenceRepository agenceRepository;

    @BeforeEach
    public void setUp() {
        // Nettoyer la base avant chaque test
        agenceRepository.deleteAll();
        
        // Créer 25 agences de test
        for (int i = 1; i <= 25; i++) {
            Agences agence = new Agences();
            agence.setCode("AG" + String.format("%03d", i));
            agence.setLabel("Agence Test " + i);
            agence.setEmail("agence" + i + "@test.com");
            agence.setTel("0522" + String.format("%06d", i));
            agence.setRegion(i % 2 == 0 ? "Casablanca-Settat" : "Rabat-Salé-Kénitra");
            agence.setAdresseComplete("Adresse " + i);
            agence.setLatitude(new java.math.BigDecimal("33.5731").add(new java.math.BigDecimal(i).multiply(new java.math.BigDecimal("0.01"))));
            agence.setLongitude(new java.math.BigDecimal("-7.5898").add(new java.math.BigDecimal(i).multiply(new java.math.BigDecimal("0.01"))));
            agence.setStatus("ACTIVE");
            agenceRepository.save(agence);
        }
    }

    @Test
    @WithMockUser
    public void testGetAgencesPaginated_DefaultParameters() throws Exception {
        mockMvc.perform(get("/agences")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(10))) // Taille par défaut
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(25)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.hasNext", is(true)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    @WithMockUser
    public void testGetAgencesPaginated_CustomPageSize() throws Exception {
        mockMvc.perform(get("/agences")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(20)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(25)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.hasNext", is(true)));
    }

    @Test
    @WithMockUser
    public void testGetAgencesPaginated_SecondPage() throws Exception {
        mockMvc.perform(get("/agences")
                .param("page", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(10)))
                .andExpect(jsonPath("$.currentPage", is(1)))
                .andExpect(jsonPath("$.hasNext", is(true)))
                .andExpect(jsonPath("$.hasPrevious", is(true)));
    }

    @Test
    @WithMockUser
    public void testGetAgencesPaginated_LastPage() throws Exception {
        mockMvc.perform(get("/agences")
                .param("page", "2")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(5))) // Reste 5 sur la dernière page
                .andExpect(jsonPath("$.currentPage", is(2)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(true)));
    }

    @Test
    @WithMockUser
    public void testGetAgencesPaginated_SortByCodeDesc() throws Exception {
        mockMvc.perform(get("/agences")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "code")
                .param("direction", "DESC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences[0].code", is("AG025")))
                .andExpect(jsonPath("$.agences[4].code", is("AG021")));
    }

    @Test
    @WithMockUser
    public void testGetAgencesPaginated_SortByLabelAsc() throws Exception {
        mockMvc.perform(get("/agences")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "label")
                .param("direction", "ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences[0].nom", is("Agence Test 1")));
    }

    @Test
    @WithMockUser
    public void testSearchAgencesByRegion() throws Exception {
        mockMvc.perform(get("/agences/search/region/Casablanca")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(10)))
                .andExpect(jsonPath("$.searchTerm", is("Casablanca")))
                .andExpect(jsonPath("$.totalItems", greaterThanOrEqualTo(12))); // 12-13 agences avec Casablanca
    }

    @Test
    @WithMockUser
    public void testSearchAgences_ByCode() throws Exception {
        mockMvc.perform(get("/agences/search")
                .param("keyword", "AG001")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.agences[0].code", containsString("AG001")))
                .andExpect(jsonPath("$.searchTerm", is("AG001")));
    }

    @Test
    @WithMockUser
    public void testSearchAgences_ByLabel() throws Exception {
        mockMvc.perform(get("/agences/search")
                .param("keyword", "Test 1")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.searchTerm", is("Test 1")));
    }

    @Test
    @WithMockUser
    public void testSearchAgences_NoResults() throws Exception {
        mockMvc.perform(get("/agences/search")
                .param("keyword", "NonExistent")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(0)))
                .andExpect(jsonPath("$.totalPages", is(0)));
    }

    @Test
    @WithMockUser
    public void testGetAllAgences_WithoutPagination() throws Exception {
        mockMvc.perform(get("/agences/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(25))); // Toutes les agences
    }

    @Test
    @WithMockUser
    public void testGetAgencesPaginated_EmptyPage() throws Exception {
        mockMvc.perform(get("/agences")
                .param("page", "100") // Page inexistante
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agences", hasSize(0)))
                .andExpect(jsonPath("$.totalItems", is(25)));
    }
}
