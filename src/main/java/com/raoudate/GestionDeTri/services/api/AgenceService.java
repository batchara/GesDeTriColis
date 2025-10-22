package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.AgenceDTO;

import java.util.List;

public interface AgenceService {

    AgenceDTO save(AgenceDTO dto);

    AgenceDTO findById(Integer id);

    List<AgenceDTO> findAll();

    List<AgenceDTO> findByCode(String code);

    List<AgenceDTO> search(String nom);

    void delete(Integer id);
}
