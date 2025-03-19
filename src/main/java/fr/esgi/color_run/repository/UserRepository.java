package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    Boolean deleteById(Long id);
    User update(User user);

}
