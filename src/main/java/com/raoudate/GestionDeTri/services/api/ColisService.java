package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import java.util.List;

public interface ColisService {

	ColisDTO save(ColisDTO colisDTO);

	ColisDTO findById(Integer id);

	List<ColisDTO> findAll();

	List<ColisDTO> searchByReference(String reference);

	ColisDTO update(Integer id, ColisDTO colisDTO);

	List<ColisDTO> search(String term);

	void delete(Integer id);
}
