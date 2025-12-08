package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.dto.response.AgenceDTO;

import java.util.List;

public interface AgenceService {

    AgenceDTO save(AgenceDTO dto);

    List<AgenceDTO> findAll();


    void delete(Integer id);
}
