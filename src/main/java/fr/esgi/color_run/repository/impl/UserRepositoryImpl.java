package fr.esgi.color_run.repository.impl;

import fr.esgi.color_run.business.User;
import fr.esgi.color_run.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final List<User> users = new ArrayList<>();

    @Override
    public User save(User user) {
        Optional<User> userOpt = findById(user.getId());

        if (userOpt.isPresent()) {
            return userOpt.get();
        }

        users.add(user);
        return user;

    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }

    @Override
    public Boolean deleteById(Long id) {
        return null;
    }

    @Override
    public User update(User user) {
        return null;
    }
}
