package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.User;
import fr.esgi.color_run.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final List<User> users = new ArrayList<>();

    @Override
    public User createUser(String pseudo, String mail, String password) {
        User user = new User();
        user.setId((long) (users.size() + 1));
        user.setName(pseudo);
        user.setEmail(mail);
        user.setPassword(password);
        users.add(user);
        return user;
    }

    @Override
    public Optional<User> connectUser(String mail, String password) {
        return users.stream()
                .filter(user -> user.getEmail().trim().equalsIgnoreCase(mail.trim()) && user.getPassword().equals(password))
                .findFirst();
    }


    @Override
    public boolean deleteUser(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        Optional<User> userOpt = getUser(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(updatedUser.getName());
            user.setFirstname(updatedUser.getFirstname());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setAddress(updatedUser.getAddress());
            user.setCity(updatedUser.getCity());
            user.setZipCode(updatedUser.getZipCode());
            user.setPositionLatitude(updatedUser.getPositionLatitude());
            user.setPositionLongitude(updatedUser.getPositionLongitude());
            return user;
        }
        return null;
    }

    @Override
    public List<User> listAllUsers() {
        return users;
    }

    @Override
    public Optional<User> getUser(Long id) {
        return users.stream().filter(user -> user.getId().equals(id)).findFirst();
    }
}
