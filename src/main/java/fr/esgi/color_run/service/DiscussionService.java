package fr.esgi.color_run.service;

import fr.esgi.color_run.business.Discussion;

import java.util.Optional;

public interface DiscussionService {
    Discussion getOrCreateForCourse(Long courseId);
    boolean toggleActiveStatus(Long courseId);
    Optional<Discussion> getDiscussionById(Long discussionId);
}