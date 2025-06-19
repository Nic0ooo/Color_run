package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Association;
import java.util.List;
import java.util.Optional;

public interface AssociationService {
    Association createAssociation(Association association);
    List<Association> getAllAssociations();
    Optional<Association> getAssociationById(Long id);
    Association updateAssociation(Long id, Association updatedAssociation);
    boolean deleteAssociation(Long id);
    List<Association> searchAssociationsByName(String name);
    Optional<Association> findByName(String name);
    List<Association> getAssociationsByCity(String city);
    boolean existsByName(String name);
}