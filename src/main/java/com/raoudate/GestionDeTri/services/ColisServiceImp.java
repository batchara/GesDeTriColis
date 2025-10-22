package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import com.raoudate.GestionDeTri.services.api.ColisService;

import java.util.List;

public class ColisServiceImp implements ColisService {

    @Override
    public ColisDTO save(ColisDTO colisDTO) {
        // TODO: implement persistence logic
        return null;
    }

    @Override
    public ColisDTO findById(Integer id) {
        // TODO: implement retrieval by id
        return null;
    }

    @Override
    public List<ColisDTO> findAll() {
        // TODO: return list of colis
        return List.of();
    }

    @Override
    public List<ColisDTO> searchByReference(String reference) {
        // TODO: implement search by reference
        return List.of();
    }

    @Override
    public ColisDTO update(Integer id, ColisDTO colisDTO) {
        // TODO: implement update
        return null;
    }

    @Override
    public List<ColisDTO> search(String term) {
        // TODO: implement general search
        return List.of();
    }

    @Override
    public void delete(Integer id) {
        // TODO: implement delete
    }
}
