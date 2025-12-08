package com.raoudate.GestionDeTri.services.impl;

import com.raoudate.GestionDeTri.dto.response.CentreDeTriDTO;
import java.util.List;

public interface CentreService {

    CentreDeTriDTO save(CentreDeTriDTO centreDeTriDTO);

    List<CentreDeTriDTO> findAll();


    CentreDeTriDTO update(Integer id, CentreDeTriDTO centreDeTriDTO); // méthode update

    void delete(Integer id);
}
