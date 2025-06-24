package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Discussion;
import fr.esgi.color_run.repository.DiscussionRepository;
import fr.esgi.color_run.repository.impl.DiscussionRepositoryImpl;
import fr.esgi.color_run.service.DiscussionService;

import java.util.Optional;

public class DiscussionServiceImpl implements DiscussionService {

    private final DiscussionRepository discussionRepository;

    public DiscussionServiceImpl() {
        this.discussionRepository = new DiscussionRepositoryImpl();
    }

    @Override
    public Discussion getOrCreateForCourse(Long courseId) {
        System.out.println("🔍 Recherche ou création de discussion pour la course: " + courseId);

        if (courseId == null) {
            System.err.println("❌ Course ID null");
            return null;
        }

        // Chercher d'abord une discussion existante
        Optional<Discussion> existingDiscussion = discussionRepository.findByCourseId(courseId);

        if (existingDiscussion.isPresent()) {
            System.out.println("✅ Discussion existante trouvée: " + existingDiscussion.get().getId());
            return existingDiscussion.get();
        }

        // Créer une nouvelle discussion si elle n'existe pas
        Discussion newDiscussion = discussionRepository.createForCourse(courseId);

        if (newDiscussion != null) {
            System.out.println("✅ Nouvelle discussion créée: " + newDiscussion.getId());
        } else {
            System.err.println("❌ Échec de la création de discussion");
        }

        return newDiscussion;
    }

    @Override
    public boolean toggleActiveStatus(Long courseId) {
        if (courseId == null) {
            return false;
        }

        Optional<Discussion> discussionOpt = discussionRepository.findByCourseId(courseId);

        if (discussionOpt.isPresent()) {
            Discussion discussion = discussionOpt.get();
            boolean newStatus = !discussion.isActive();

            Discussion updatedDiscussion = discussionRepository.updateActiveStatus(discussion.getId(), newStatus);

            if (updatedDiscussion != null) {
                String status = newStatus ? "activée" : "désactivée";
                System.out.println("✅ Discussion " + status + " pour la course: " + courseId);
                return true;
            }
        }

        System.err.println("❌ Impossible de basculer le statut de la discussion pour la course: " + courseId);
        return false;
    }

    @Override
    public Optional<Discussion> getDiscussionById(Long discussionId) {
        if (discussionId == null) {
            return Optional.empty();
        }

        return discussionRepository.findById(discussionId);
    }
}