package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Association;

import java.util.List;
import java.util.Optional;

public interface AssociationRepository {

    List<Association> findAll();

    Optional<Association> findById(Long id);

    Association save(Association association);

    Association update(Association association);

    boolean deleteById(Long id);

    Optional<Association> findByName(String name);

    Association findByEmail(String email);

    List<Association> findByCity(String city);

    boolean existsByName(String name);

    boolean existsByEmail(String email);
}
