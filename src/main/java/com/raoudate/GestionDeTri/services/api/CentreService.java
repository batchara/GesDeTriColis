package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.CentreDeTriDTO;
import java.util.List;

public interface CentreService {

    CentreDeTriDTO save(CentreDeTriDTO centreDeTriDTO);

    List<CentreDeTriDTO> findAll();


    CentreDeTriDTO update(Integer id, CentreDeTriDTO centreDeTriDTO); // méthode update

    void delete(Integer id);
}
