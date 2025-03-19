package fr.esgi.color_run.service;

import fr.esgi.color_run.business.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(String pseudo, String mail, String password);
    Optional<User> connectUser(String mail, String password);
    boolean deleteUser(Long id);
    User updateUser(Long id, User updatedUser);
    List<User> listAllUsers();
    Optional<User> getUser(Long id);
}
