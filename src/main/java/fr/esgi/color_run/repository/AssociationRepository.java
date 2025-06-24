package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Association;

import java.util.List;

public interface AssociationRepository {

    List<Association> findAll();

    Association findById(Long id);
}
