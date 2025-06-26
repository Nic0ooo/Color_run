package fr.esgi.color_run.util;

import fr.esgi.color_run.repository.*;
import fr.esgi.color_run.repository.impl.*;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.service.impl.GeocodingServiceImpl;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory singleton pour les repositories afin d'éviter les créations multiples
 */
public class RepositoryFactory {

    private static RepositoryFactory instance;
    private static final Object lock = new Object();

    private final ConcurrentHashMap<Class<?>, Object> repositories = new ConcurrentHashMap<>();

    private RepositoryFactory() {
        System.out.println("✅ RepositoryFactory initialisé");
    }

    public static RepositoryFactory getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new RepositoryFactory();
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T getRepository(Class<T> repositoryInterface) {
        return (T) repositories.computeIfAbsent(repositoryInterface, this::createRepository);
    }

    private Object createRepository(Class<?> repositoryInterface) {
        if (repositoryInterface == AssociationRepository.class) {
            return new AssociationRepositoryImpl();
        } else if (repositoryInterface == Association_memberRepository.class) {
            return new Association_memberRepositoryImpl();
        } else if (repositoryInterface == MemberRepository.class) {
            return new MemberRepositoryImpl();
        } else if (repositoryInterface == OrganizerRequestRepository.class) {
            return new OrganizerRequestRepositoryImpl();
        } else if (repositoryInterface == CourseRepository.class) {
            // Assuming CourseRepository exists
            return new CourseRepositoryImpl(new GeocodingServiceImpl());
        }

        throw new IllegalArgumentException("Repository non supporté: " + repositoryInterface.getName());
    }

    // Getters spécifiques pour éviter les cast
    public AssociationRepository getAssociationRepository() {
        return getRepository(AssociationRepository.class);
    }

    public Association_memberRepository getAssociationMemberRepository() {
        return getRepository(Association_memberRepository.class);
    }

    public MemberRepository getMemberRepository() {
        return getRepository(MemberRepository.class);
    }

    public OrganizerRequestRepository getOrganizerRequestRepository() {
        return getRepository(OrganizerRequestRepository.class);
    }

    public CourseRepository getCourseRepository(GeocodingService geocodingService) {
        return getRepository(CourseRepository.class);
    }
}