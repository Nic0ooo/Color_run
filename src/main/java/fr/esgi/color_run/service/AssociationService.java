package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Association;
import java.util.List;
import java.util.Optional;

public interface AssociationService {

    /**
     * Créer une nouvelle association
     */
    Long createAssociation(String name, String email, String description,
                           String websiteLink, String phone, String address,
                           String zipCode, String city) throws Exception;

    /**
     * Récupérer toutes les associations
     */
    List<Association> getAllAssociations() throws Exception;

    /**
     * Récupérer une association par son ID
     */
    Optional<Association> getAssociationById(Long id) throws Exception;

    /**
     * Mettre à jour une association
     */
    void updateAssociation(Association association) throws Exception;

    /**
     * Supprimer une association
     */
    void deleteAssociation(Long id) throws Exception;

    /**
     * Rechercher des associations par nom
     */
    List<Association> searchAssociationsByName(String name) throws Exception;

    Optional<Association> findById(Long id);

    /**
     * Vérifier si une association existe par email
     */
    boolean existsByEmail(String email) throws Exception;

    /**
     * Vérifier si une association existe par nom
     */
    boolean existsByName(String name) throws Exception;
}