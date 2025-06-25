package fr.esgi.color_run.repository;

import fr.esgi.color_run.business.Discussion;
import java.util.Optional;

public interface DiscussionRepository {

    /**
     * Trouve la discussion d'une course
     */
    Optional<Discussion> findByCourseId(Long courseId);

    /**
     * Crée une discussion pour une course
     */
    Discussion createForCourse(Long courseId);

    /**
     * Active/désactive une discussion
     */
    Discussion updateActiveStatus(Long discussionId, boolean isActive);

    /**
     * Trouve une discussion par son ID
     */
    Optional<Discussion> findById(Long id);
}
