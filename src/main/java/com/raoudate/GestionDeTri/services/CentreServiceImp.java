package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.CentreDeTriDTO;
import com.raoudate.GestionDeTri.services.api.CentreService;

import java.util.List;

public class CentreServiceImp implements CentreService {

    @Override
    public CentreDeTriDTO save(CentreDeTriDTO centreDeTriDTO) {
        return null;
    }

    @Override
    public CentreDeTriDTO findById(Integer id) {
        return null;
    }

    @Override
    public CentreDeTriDTO findByIdAndName(Integer id, String name) {
        return null;
    }

    @Override
    public List<CentreDeTriDTO> findAll() {
        return List.of();
    }

    @Override
    public List<CentreDeTriDTO> searchByNom(String nom) {
        return List.of();
    }

    @Override
    public CentreDeTriDTO update(Integer id, CentreDeTriDTO centreDeTriDTO) {
        return null;
    }

    @Override
    public List<CentreDeTriDTO> search(String term) {
        return List.of();
    }

    @Override
    public void delete(Integer id) {

    }
}
