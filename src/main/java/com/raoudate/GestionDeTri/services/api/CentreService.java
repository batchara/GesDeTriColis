package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.CentreDeTriDTO;
import java.util.List;

public interface CentreService {

    CentreDeTriDTO save(CentreDeTriDTO centreDeTriDTO);

    CentreDeTriDTO findById(Integer id);

    CentreDeTriDTO findByIdAndName(Integer id, String name);

    List<CentreDeTriDTO> findAll();

    List<CentreDeTriDTO> searchByNom(String nom);

    CentreDeTriDTO update(Integer id, CentreDeTriDTO centreDeTriDTO); // méthode update

    List<CentreDeTriDTO> search(String term); // recherche par id, ou nom/prenom

    void delete(Integer id);
}
