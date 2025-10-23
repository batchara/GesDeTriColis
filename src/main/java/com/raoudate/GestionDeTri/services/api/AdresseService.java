package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.AdresseDTO;
import java.util.List;

public interface AdresseService {

	AdresseDTO save (AdresseDTO adresseDTO);


    List<AdresseDTO> findAll();

    List<AdresseDTO> searchByCity(String city);

    AdresseDTO update(Integer id, AdresseDTO adresseDTO);

    List<AdresseDTO> search(String code);
    
    void delete(Integer id);
}
