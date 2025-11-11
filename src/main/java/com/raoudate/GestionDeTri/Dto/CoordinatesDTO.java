package com.raoudate.GestionDeTri.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour représenter des coordonnées GPS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesDTO {
    private Double latitude;
    private Double longitude;
}
