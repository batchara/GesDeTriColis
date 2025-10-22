package com.raoudate.GestionDeTri.services.api;

import com.raoudate.GestionDeTri.Dto.UserDTO;
import java.util.List;

public interface UserService {

	UserDTO save(UserDTO userDTO);

	UserDTO findById(Integer id);

	UserDTO findByUsername(String username);

	List<UserDTO> findAll();

	List<UserDTO> searchByEmail(String email);

	UserDTO update(Integer id, UserDTO userDTO);

	List<UserDTO> search(String term);

	void delete(Integer id);
}
