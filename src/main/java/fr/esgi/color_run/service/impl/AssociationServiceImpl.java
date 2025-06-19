package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.repository.AssociationRepository;
import fr.esgi.color_run.repository.impl.AssociationRepositoryImpl;
import fr.esgi.color_run.service.AssociationService;

import java.util.List;
import java.util.Optional;

public class AssociationServiceImpl implements AssociationService {

    private final AssociationRepository associationRepository = new AssociationRepositoryImpl();

    @Override
    public Association createAssociation(Association association) {
        // Validation
        if (association.getName() == null || association.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'association est obligatoire");
        }

        if (existsByName(association.getName().trim())) {
            throw new IllegalArgumentException("Une association avec ce nom existe déjà");
        }

        // Nettoyer les données
        association.setName(association.getName().trim());
        if (association.getDescription() != null) {
            association.setDescription(association.getDescription().trim());
        }
        if (association.getEmail() != null) {
            association.setEmail(association.getEmail().trim().toLowerCase());
        }

        return associationRepository.save(association);
    }

    @Override
    public List<Association> getAllAssociations() {
        return associationRepository.findAll();
    }

    @Override
    public Optional<Association> getAssociationById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Association association = associationRepository.findById(id);
        return association != null ? Optional.of(association) : Optional.empty();
    }

    @Override
    public Association updateAssociation(Long id, Association updatedAssociation) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'association est obligatoire");
        }

        Association existingAssociation = associationRepository.findById(id);
        if (existingAssociation == null) {
            throw new IllegalArgumentException("Association introuvable avec l'ID : " + id);
        }

        // Vérifier l'unicité du nom si changé
        if (!existingAssociation.getName().equalsIgnoreCase(updatedAssociation.getName())
                && existsByName(updatedAssociation.getName())) {
            throw new IllegalArgumentException("Une association avec ce nom existe déjà");
        }

        // Nettoyer les données
        updatedAssociation.setId(id);
        updatedAssociation.setName(updatedAssociation.getName().trim());
        if (updatedAssociation.getDescription() != null) {
            updatedAssociation.setDescription(updatedAssociation.getDescription().trim());
        }
        if (updatedAssociation.getEmail() != null) {
            updatedAssociation.setEmail(updatedAssociation.getEmail().trim().toLowerCase());
        }

        return associationRepository.update(updatedAssociation);
    }

    @Override
    public boolean deleteAssociation(Long id) {
        if (id == null) {
            return false;
        }

        Association association = associationRepository.findById(id);
        if (association == null) {
            return false;
        }

        // TODO: Vérifier qu'aucune course n'est liée à cette association
        // TODO: Vérifier qu'aucun organisateur n'est lié à cette association

        return associationRepository.deleteById(id);
    }

    @Override
    public List<Association> searchAssociationsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllAssociations();
        }
        return associationRepository.findByNameContaining(name.trim());
    }

    @Override
    public Optional<Association> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return associationRepository.findByName(name.trim());
    }

    @Override
    public List<Association> getAssociationsByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return getAllAssociations();
        }
        return associationRepository.findByCity(city.trim());
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return associationRepository.existsByName(name.trim());
    }
}
