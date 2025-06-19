package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Association;

import java.util.List;
import java.util.Optional;

public interface AssociationRepository {

    List<Association> findAll();

    Association findById(Long id);

    Association save(Association association);

    Association update(Association association);

    boolean deleteById(Long id);

    List<Association> findByNameContaining(String name);

    Optional<Association> findByName(String name);

    List<Association> findByCity(String city);

    boolean existsByName(String name);
}
