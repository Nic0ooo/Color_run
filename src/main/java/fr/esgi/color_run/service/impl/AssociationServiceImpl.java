package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Association;
import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.AssociationRepository;
import fr.esgi.color_run.repository.Association_memberRepository;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.AssociationRepositoryImpl;
import fr.esgi.color_run.repository.impl.Association_memberRepositoryImpl;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.AssociationService;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.util.RepositoryFactory;

import java.util.List;
import java.util.Optional;

public class AssociationServiceImpl implements AssociationService {

    private final AssociationRepository associationRepository;
    private final Association_memberRepository association_memberRepository;
    private final GeocodingService geocodingService = new GeocodingServiceImpl();
    private final CourseRepository courseRepository = new CourseRepositoryImpl(geocodingService);

    public AssociationServiceImpl() {
        RepositoryFactory factory = RepositoryFactory.getInstance();
        this.associationRepository = factory.getAssociationRepository();
        this.association_memberRepository = factory.getAssociationMemberRepository();
    }

    @Override
    public Long createAssociation(String name, String email, String description, String websiteLink, String phone, String address, String zipCode, String city) {
        System.out.println("🔍 Service - Création d'association: " + name);

        // Validation des données obligatoires
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'association est obligatoire");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email de l'association est obligatoire");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La description de l'association est obligatoire");
        }

        // Créer l'association
        Association association = new Association();
        association.setName(name.trim());
        association.setEmail(email.trim());
        association.setDescription(description.trim());
        association.setWebsiteLink(websiteLink != null ? websiteLink.trim() : null);
        association.setPhoneNumber(phone != null ? phone.trim() : null);
        association.setAddress(address != null ? address.trim() : null);
        association.setCity(city != null ? city.trim() : null);

        // Convertir zipCode en entier si fourni
        if (zipCode != null && !zipCode.trim().isEmpty()) {
            try {
                association.setZipCode(Integer.parseInt(zipCode.trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Code postal invalide: " + zipCode);
            }
        }

        // Sauvegarder
        associationRepository.save(association);

        System.out.println("✅ Service - Association créée avec ID: " + association.getId());
        return association.getId();
    }

    @Override
    public List<Association> getAllAssociations() {
        System.out.println("🔍 Service - Récupération de toutes les associations");
        List<Association> associations = associationRepository.findAll();
        System.out.println("✅ Service - " + associations.size() + " associations trouvées");
        return associations;
    }

    @Override
    public Optional<Association> getAssociationById(Long id) {
        return associationRepository.findById(id);
    }

    @Override
    public void updateAssociation(Association association) {
        System.out.println("🔍 Service - Mise à jour association ID: " + association.getId());

        if (association.getId() == null) {
            throw new IllegalArgumentException("ID de l'association requis pour la mise à jour");
        }

        // Vérifier que l'association existe
        Optional<Association> existing = associationRepository.findById(association.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Association non trouvée avec ID: " + association.getId());
        }

        // Validation des données obligatoires
        if (association.getName() == null || association.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'association est obligatoire");
        }

        if (association.getEmail() == null || association.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email de l'association est obligatoire");
        }

        // Vérifier l'unicité de l'email (sauf pour la même association)
        Association emailCheck = associationRepository.findByEmail(association.getEmail().trim());
        if (emailCheck != null && !emailCheck.getId().equals(association.getId())) {
            throw new IllegalArgumentException("Une autre association avec cet email existe déjà");
        }

        // Mettre à jour
        associationRepository.update(association);

        System.out.println("✅ Service - Association " + association.getId() + " mise à jour");
    }

    @Override
    public void deleteAssociation(Long id) {
        System.out.println("🔍 Service - Suppression association ID: " + id);

        // Vérifier que l'association existe
        Optional<Association> associationOpt = associationRepository.findById(id);
        if (associationOpt.isEmpty()) {
            throw new IllegalArgumentException("Association non trouvée avec ID: " + id);
        }
        Association association = associationOpt.get();

        // Vérifier s'il y a des courses à venir liés à cette association
        List<Course> upcomingAssoCourse = courseRepository.findUpcomingCoursesByAssociationId(id);
        if (upcomingAssoCourse != null && !upcomingAssoCourse.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer cette association car des courses à venir y sont associées");
        }

        // Vérifier s'il y a des courses passés liés à cette association
        List<Course> pastAssoCourse = courseRepository.findPastCoursesByAssociationId(id);
        if (pastAssoCourse != null && !pastAssoCourse.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer cette association car des courses passées y sont associées");
        }

        // Vérifier s'il y a des membres liés à cette association
        List<Member> members = association_memberRepository.findOrganizersByAssociationId(id);
        if (members.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer cette association car des membres y sont associés");
        }

        associationRepository.deleteById(id);

        System.out.println("✅ Service - Association " + id + " supprimée");
    }

@Override
public List<Association> searchAssociationsByName(String name) {
    if (name == null || name.trim().isEmpty()) {
        return getAllAssociations();
    }

    List<Association> associations = null;
    try {
        System.out.println("🔍 Service - Recherche associations par nom: " + name);
        // Création d'une liste à partir du résultat Optional
        Optional<Association> associationOpt = associationRepository.findByName(name.trim());
        associations = associationOpt.isPresent() ?
            List.of(associationOpt.get()) : List.of();

    } catch (Exception e) {
        System.err.println("❌ Erreur dans searchAssociationsByName:");
        e.printStackTrace();
        throw e;
    }
    System.out.println("✅ Service - " + associations.size() + " associations trouvées pour: " + name);
    return associations;
}

    @Override
    public Optional<Association> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return associationRepository.findById(id);
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return associationRepository.existsByName(name.trim());
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return associationRepository.existsByEmail(email.trim().toLowerCase());
    }
}
