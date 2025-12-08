package com.raoudate.GestionDeTri.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeStatsDTO {
    private Long totalColis;
    private Long colisEnAttente;
    private Long colisLivres;
    private Long totalAgences;
    private Long totalUtilisateurs;
}
