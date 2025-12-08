package com.raoudate.GestionDeTri.services.impl;

import com.raoudate.GestionDeTri.dto.response.ColisDTO;
import com.raoudate.GestionDeTri.model.Colis;
import java.util.List;

public interface ColisService {

	ColisDTO save(ColisDTO colisDTO);

	List<ColisDTO> findAll();

	ColisDTO findById(Integer id);

	/**
	 * Récupérer l'entité Colis directement (pour usage interne)
	 */
	Colis findEntityById(Integer id);

	ColisDTO update(Integer id, ColisDTO colisDTO);

	void delete(Integer id);
	
	/**
	 * Supprimer plusieurs colis en une seule opération (soft delete)
	 * @param ids Liste des IDs des colis à supprimer
	 * @return Nombre de colis supprimés avec succès
	 */
	int deleteMultiple(List<Integer> ids);
	
	/**
	 * Créer une demande de suppression (génère une notification pour l'admin)
	 * @param id ID du colis à supprimer
	 * @return La notification créée
	 */
	com.raoudate.GestionDeTri.dto.response.NotificationDTO requestDeletion(Integer id);
}
