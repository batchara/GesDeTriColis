package com.raoudate.GestionDeTri.services;
import com.raoudate.GestionDeTri.Dto.UserDTO;
import com.raoudate.GestionDeTri.auth.ChangePasswordRequest;
import com.raoudate.GestionDeTri.model.User;
import com.raoudate.GestionDeTri.repository.UserRepository;
import com.raoudate.GestionDeTri.services.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        repository.save(user);
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    /**
     * Récupérer un utilisateur par son ID
     */
    public User getUserById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur avec l'ID " + id + " introuvable"));
    }

    @Override
    public UserDTO save(UserDTO userDTO) {
        return null;
    }

    @Override
    public UserDTO findById(Integer id) {
        return null;
    }

    @Override
    public UserDTO findByUsername(String username) {
        return null;
    }

    @Override
    public List<UserDTO> findAll() {
        return List.of();
    }

    @Override
    public List<UserDTO> searchByEmail(String email) {
        return List.of();
    }

    @Override
    public UserDTO update(Integer id, UserDTO userDTO) {
        return null;
    }

    @Override
    public List<UserDTO> search(String term) {
        return List.of();
    }

    @Override
    public void delete(Integer id) {

    }
}