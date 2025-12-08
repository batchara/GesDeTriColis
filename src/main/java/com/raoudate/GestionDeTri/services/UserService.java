package com.raoudate.GestionDeTri.services;

import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.dto.request.CreateUserRequest;
import com.raoudate.GestionDeTri.dto.response.UserDTO;
import com.raoudate.GestionDeTri.model.User;

import java.security.Principal;
import java.util.List;

public interface UserService {

	// Gestion du changement de mot de passe
	void changePassword(ChangePasswordRequest request, Principal connectedUser);

	// Récupération des utilisateurs
	List<User> getAllUsers();
	List<User> searchUsers(String searchTerm);
	User getUserById(Integer id);
	User getUserByEmail(String email);
	boolean existsByEmail(String email);

	// Création et mise à jour
	User createUser(CreateUserRequest request);
	User updateUser(Integer id, CreateUserRequest request);
	User updateUserRoles(Integer id, List<String> roleNames);
	User updateUserPersonalInfo(Integer id, CreateUserRequest request);

	// Suppression et restauration
	void deleteUser(Integer id);
	User restoreUser(Integer id);

	// Activation/Désactivation
	void enableUser(Integer id);
	void disableUser(Integer id);

	// Verrouillage/Déverrouillage
	void lockUser(Integer id);
	void unlockUser(Integer id);

	// Méthodes legacy (DTO)
	UserDTO save(UserDTO userDTO);
	UserDTO findById(Integer id);
	UserDTO findByUsername(String username);
	List<UserDTO> findAll();
	List<UserDTO> searchByEmail(String email);
	UserDTO update(Integer id, UserDTO userDTO);
	List<UserDTO> search(String term);
	void delete(Integer id);
}
