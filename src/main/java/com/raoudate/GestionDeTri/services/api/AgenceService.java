package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.AgenceDTO;

import java.util.List;

public interface AgenceService {

    AgenceDTO save(AgenceDTO dto);

    List<AgenceDTO> findAll();


    void delete(Integer id);
}
