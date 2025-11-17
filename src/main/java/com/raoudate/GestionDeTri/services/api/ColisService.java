package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.ColisDTO;
import java.util.List;

public interface ColisService {

	ColisDTO save(ColisDTO colisDTO);

	List<ColisDTO> findAll();

	ColisDTO findById(Integer id);

	ColisDTO update(Integer id, ColisDTO colisDTO);

	void delete(Integer id);
	
	/**
	 * Supprimer plusieurs colis en une seule opération (soft delete)
	 * @param ids Liste des IDs des colis à supprimer
	 * @return Nombre de colis supprimés avec succès
	 */
	int deleteMultiple(List<Integer> ids);
}
