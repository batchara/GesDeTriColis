package com.raoudate.GestionDeTri.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO pour la requête de scan d'un colis (OCR + Géocodage)
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanColisRequestDTO {
    private MultipartFile image;
    private String adresseManuelle; 
}
