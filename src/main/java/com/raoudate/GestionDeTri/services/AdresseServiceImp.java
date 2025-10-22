package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.Dto.AdresseDTO;
import com.raoudate.GestionDeTri.services.api.AdresseService;

import java.util.List;

public class AdresseServiceImp implements AdresseService {

    @Override
    public AdresseDTO save(AdresseDTO adresseDTO) {
        return null;
    }

    @Override
    public AdresseDTO findById(Integer id) {
        return null;
    }

    @Override
    public List<AdresseDTO> findAll() {
        return List.of();
    }

    @Override
    public List<AdresseDTO> searchByCity(String city) {
        return List.of();
    }

    @Override
    public AdresseDTO update(Integer id, AdresseDTO adresseDTO) {
        return null;
    }

    @Override
    public List<AdresseDTO> search(String code) {
        return List.of();
    }

    @Override
    public void delete(Integer id) {

    }
}
