package fr.esgi.color_run.service.impl;

import fr.esgi.color_run.business.Discussion;
import fr.esgi.color_run.business.Message;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.repository.MessageRepository;
import fr.esgi.color_run.repository.impl.MessageRepositoryImpl;
import fr.esgi.color_run.service.DiscussionService;
import fr.esgi.color_run.service.MessageService;
import fr.esgi.color_run.service.Course_memberService;
import fr.esgi.color_run.service.MemberService;
import fr.esgi.color_run.service.impl.Course_memberServiceImpl;
import fr.esgi.color_run.service.impl.MemberServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final DiscussionService discussionService;
    private final Course_memberService courseMemberService;
    private final MemberService memberService;

    public MessageServiceImpl() {
        this.messageRepository = new MessageRepositoryImpl();
        this.discussionService = new DiscussionServiceImpl();
        this.courseMemberService = new Course_memberServiceImpl();
        this.memberService = new MemberServiceImpl();
    }

    @Override
    public Message sendMessage(Long courseId, Long memberId, String content) {
        System.out.println("💬 Tentative d'envoi de message - Course: " + courseId + ", Member: " + memberId);

        // Vérifications de sécurité
        if (!canAccessChat(courseId, memberId)) {
            System.err.println("❌ Membre " + memberId + " non autorisé à envoyer des messages dans la course " + courseId);
            return null;
        }

        // Validation du contenu
        if (content == null || content.trim().isEmpty()) {
            System.err.println("❌ Contenu du message vide");
            return null;
        }

        content = content.trim();
        if (content.length() > 1000) {
            System.err.println("❌ Message trop long: " + content.length() + " caractères");
            return null;
        }

        // 1. Trouver ou créer la discussion pour cette course
        Discussion discussion = discussionService.getOrCreateForCourse(courseId);
        if (discussion == null) {
            System.err.println("❌ Impossible de créer/récupérer la discussion pour la course " + courseId);
            return null;
        }

        // 2. Créer le message avec discussionId
        Message message = new Message(discussion.getId(), memberId, content);
        message.sanitizeContent();

        if (!message.isValid()) {
            System.err.println("❌ Message invalide après validation");
            return null;
        }

        // 3. Sauvegarder
        Message savedMessage = messageRepository.save(message);
        if (savedMessage != null) {
            System.out.println("✅ Message envoyé avec succès - ID: " + savedMessage.getId());
        }

        return savedMessage;
    }

    @Override
    public List<Message> getCourseMessages(Long courseId, Long memberId) {
        System.out.println("📥 Récupération des messages - Course: " + courseId + ", Member: " + memberId);

        // Vérification des autorisations
        if (!canAccessChat(courseId, memberId)) {
            System.err.println("❌ Membre " + memberId + " non autorisé à lire les messages de la course " + courseId);
            return List.of();
        }

        // Trouver la discussion pour cette course
        Discussion discussion = discussionService.getOrCreateForCourse(courseId);
        if (discussion == null) {
            System.err.println("❌ Aucune discussion trouvée pour la course " + courseId);
            return List.of();
        }

        // Créer un message de bienvenue si c'est la première fois
        createWelcomeMessageIfNeeded(courseId, memberId);

        // Récupération des messages récents (100 derniers)
        List<Message> messages = messageRepository.findRecentByDiscussionId(discussion.getId(), 100);

        System.out.println("📨 " + messages.size() + " messages récupérés pour la course " + courseId);
        return messages;
    }

    @Override
    public List<Message> getNewMessages(Long courseId, Long memberId, Long sinceMessageId) {
        System.out.println("🆕 Récupération des nouveaux messages - Course: " + courseId +
                ", Member: " + memberId + ", Since: " + sinceMessageId);

        // Vérification des autorisations
        if (!canAccessChat(courseId, memberId)) {
            System.err.println("❌ Membre " + memberId + " non autorisé à lire les nouveaux messages de la course " + courseId);
            return List.of();
        }

        // Trouver la discussion pour cette course
        Discussion discussion = discussionService.getOrCreateForCourse(courseId);
        if (discussion == null) {
            System.err.println("❌ Aucune discussion trouvée pour la course " + courseId);
            return List.of();
        }

        // Récupération des nouveaux messages
        List<Message> newMessages = messageRepository.findByDiscussionIdSinceId(discussion.getId(), sinceMessageId);

        System.out.println("📨 " + newMessages.size() + " nouveaux messages récupérés");
        return newMessages;
    }

    @Override
    public boolean canAccessChat(Long courseId, Long memberId) {
        if (courseId == null || memberId == null) {
            return false;
        }

        try {
            Optional<Member> memberOpt = memberService.getMember(memberId);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                Role memberRole = member.getRole();

                // ✅ ADMIN et ORGANIZER peuvent accéder à tous les chats sans inscription
                if (memberRole == Role.ADMIN || memberRole == Role.ORGANIZER) {
                    System.out.println("✅ Accès chat autorisé pour modérateur " + memberId + " (" + memberRole + ") sur course " + courseId);
                    return true;
                }
            }

            // Vérifier que le membre est inscrit ET a payé sa course
            boolean isRegisteredAndPaid = courseMemberService.isMemberRegisteredAndPaid(courseId, memberId);

            System.out.println("🔐 Vérification accès chat - Course: " + courseId +
                    ", Member: " + memberId + ", Autorisé: " + isRegisteredAndPaid);

            return isRegisteredAndPaid;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification d'accès au chat:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean togglePinMessage(Long messageId, Long moderatorId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (!messageOpt.isPresent()) {
            System.err.println("❌ Message non trouvé: " + messageId);
            return false;
        }

        Message message = messageOpt.get();

        // Récupérer le courseId via la discussion
        Long courseId = getCourseIdFromMessage(message);
        if (courseId == null) {
            System.err.println("❌ Impossible de récupérer le courseId pour le message: " + messageId);
            return false;
        }

        if (!isModerator(courseId, moderatorId)) {
            System.err.println("❌ Utilisateur " + moderatorId + " n'est pas modérateur de la course " + courseId);
            return false;
        }

        try {
            messageRepository.togglePin(messageId);
            System.out.println("✅ Pin basculé pour le message: " + messageId);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du basculement du pin:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean hideMessage(Long messageId, Long moderatorId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (!messageOpt.isPresent()) {
            System.err.println("❌ Message non trouvé: " + messageId);
            return false;
        }

        Message message = messageOpt.get();

        // Récupérer le courseId via la discussion
        Long courseId = getCourseIdFromMessage(message);
        if (courseId == null) {
            System.err.println("❌ Impossible de récupérer le courseId pour le message: " + messageId);
            return false;
        }

        if (!isModerator(courseId, moderatorId)) {
            System.err.println("❌ Utilisateur " + moderatorId + " n'est pas modérateur de la course " + courseId);
            return false;
        }

        try {
            // Sauvegarder le contenu original si ce n'est pas déjà fait
            message.preserveOriginalContent();

            // Marquer comme masqué
            message.markAsHiddenByModerator(moderatorId);

            Message updatedMessage = messageRepository.update(message);

            if (updatedMessage != null) {
                System.out.println("✅ Message masqué par modérateur " + moderatorId + " - ID: " + messageId);
                return true;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du masquage du message " + messageId + ":");
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteMessage(Long messageId, Long requesterId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (!messageOpt.isPresent()) {
            System.err.println("❌ Message non trouvé: " + messageId);
            return false;
        }

        Message message = messageOpt.get();

        // Récupérer le courseId via la discussion
        Long courseId = getCourseIdFromMessage(message);
        if (courseId == null) {
            System.err.println("❌ Impossible de récupérer le courseId pour le message: " + messageId);
            return false;
        }

        // ✅ RÉCUPÉRER LE MEMBRE POUR VÉRIFIER SON RÔLE
        Optional<Member> memberOpt = memberService.getMember(requesterId);
        if (!memberOpt.isPresent()) {
            System.err.println("❌ Membre non trouvé avec ID: " + requesterId);
            return false;
        }

        Member requester = memberOpt.get();
        boolean isAuthor = message.getMemberId().equals(requesterId);

        // ✅ VÉRIFICATION SPÉCIFIQUE : Seuls les ADMIN peuvent supprimer définitivement
        boolean isAdmin = (requester.getRole() == Role.ADMIN);
        boolean isMod = isModerator(courseId, requesterId);
        // Vérifier que c'est l'auteur du message ou un modérateur
//        boolean isAuthor = message.getMemberId().equals(requesterId);
//        boolean isMod = isModerator(courseId, requesterId);

        if (!isAuthor && !isMod) {
            System.err.println("❌ Utilisateur " + requesterId + " n'a pas le droit de supprimer le message " + messageId);
            return false;
        }

        try {
            if (isMod && !isAuthor) {
                if (!isAdmin) {
                    System.err.println("❌ Utilisateur " + requesterId + " (" + requester.getRole() + ") ne peut pas supprimer définitivement. Seuls les ADMIN le peuvent.");
                    return false;
                }
                // Suppression définitive par un modérateur
                messageRepository.delete(messageId);
                System.out.println("✅ Message supprimé définitivement par modérateur " + requesterId + " - ID: " + messageId);
            } else {
                // Suppression par l'auteur : marquer comme supprimé
                message.markAsDeletedByAuthor();
                Message updatedMessage = messageRepository.update(message);

                if (updatedMessage != null) {
                    System.out.println("✅ Message supprimé par l'auteur " + requesterId + " - ID: " + messageId);
                } else {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du message " + messageId + ":");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isModerator(Long courseId, Long memberId) {
        if (courseId == null || memberId == null) {
            System.err.println("❌ courseId ou memberId null dans isModerator");
            return false;
        }

        try {
            Optional<Member> memberOpt = memberService.getMember(memberId);
            if (!memberOpt.isPresent()) {
                System.err.println("❌ Membre non trouvé avec ID: " + memberId);
                return false;
            }

            Member member = memberOpt.get();
            Role memberRole = member.getRole();

            System.out.println("🔍 Vérification modérateur:");
            System.out.println("  - Member ID: " + memberId);
            System.out.println("  - Member Role: " + memberRole);
            System.out.println("  - Course ID: " + courseId);

            // Les admins et organisateurs sont modérateurs de toutes les courses
            boolean isMod = (memberRole == Role.ADMIN || memberRole == Role.ORGANIZER);

            System.out.println("  - Est modérateur: " + isMod);
            return isMod;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification du statut modérateur:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Méthode utilitaire pour récupérer le courseId depuis un message
     * via sa discussion
     */
    private Long getCourseIdFromMessage(Message message) {
        try {
            Optional<Discussion> discussionOpt = discussionService.getDiscussionById(message.getDiscussionId());
            if (discussionOpt.isPresent()) {
                return discussionOpt.get().getCourseId();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du courseId:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Crée un message de bienvenue automatique quand un membre accède au chat pour la première fois
     */
    public void createWelcomeMessageIfNeeded(Long courseId, Long memberId) {
        System.out.println("🎉 Vérification message de bienvenue - Course: " + courseId + ", Member: " + memberId);

        try {
            // Vérifier s'il y a déjà des messages dans cette discussion
            Discussion discussion = discussionService.getOrCreateForCourse(courseId);
            if (discussion == null) {
                return;
            }

            int messageCount = messageRepository.countByDiscussionId(discussion.getId());

            // Si c'est le premier message de la discussion, créer un message de bienvenue
            if (messageCount == 0) {
                String welcomeContent = "🏃‍♂️ Bienvenue dans le chat de la course ! " +
                        "Merci pour votre inscription. Vous pouvez désormais échanger avec les autres participants. " +
                        "N'hésitez pas à poser vos questions ou à partager votre enthousiasme !";

                Message welcomeMessage = new Message(discussion.getId(), 1L, welcomeContent); // ID 1 = organisateur
                welcomeMessage.sanitizeContent();

                Message savedMessage = messageRepository.save(welcomeMessage);
                if (savedMessage != null) {
                    System.out.println("✅ Message de bienvenue créé pour la course " + courseId);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du message de bienvenue:");
            e.printStackTrace();
        }
    }

    /**
     * Met à jour un message existant
     */
    public Message updateMessage(Long messageId, Long memberId, String newContent) {
        System.out.println("✏️ Tentative de modification - Message: " + messageId + ", Member: " + memberId);

        // Validation du contenu
        if (newContent == null || newContent.trim().isEmpty()) {
            System.err.println("❌ Nouveau contenu vide");
            return null;
        }

        newContent = newContent.trim();
        if (newContent.length() > 1000) {
            System.err.println("❌ Nouveau contenu trop long: " + newContent.length() + " caractères");
            return null;
        }

        // Récupérer le message
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (!messageOpt.isPresent()) {
            System.err.println("❌ Message non trouvé: " + messageId);
            return null;
        }

        Message message = messageOpt.get();

        // Vérifier que c'est bien le message de l'utilisateur
        if (!message.getMemberId().equals(memberId)) {
            System.err.println("❌ Utilisateur " + memberId + " ne peut pas modifier le message " + messageId + " (appartient à " + message.getMemberId() + ")");
            return null;
        }

        // Vérifier que le message peut être modifié
        if (!message.canBeEdited()) {
            System.err.println("❌ Message " + messageId + " ne peut pas être modifié (supprimé ou masqué)");
            return null;
        }

        try {
            // Sauvegarder le contenu original si ce n'est pas déjà fait
            message.preserveOriginalContent();

            // Mettre à jour le contenu
            message.setContent(newContent);
            message.markAsModified();
            message.sanitizeContent();

            Message updatedMessage = messageRepository.update(message);

            if (updatedMessage != null) {
                System.out.println("✅ Message modifié avec succès - ID: " + messageId);
                return updatedMessage;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la modification du message " + messageId + ":");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Supprime un message par son auteur
     */
    public boolean deleteOwnMessage(Long messageId, Long memberId) {
        System.out.println("🗑️ Tentative de suppression par l'auteur - Message: " + messageId + ", Member: " + memberId);

        // Récupérer le message
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (!messageOpt.isPresent()) {
            System.err.println("❌ Message non trouvé: " + messageId);
            return false;
        }

        Message message = messageOpt.get();

        // Vérifier que c'est bien le message de l'utilisateur
        if (!message.getMemberId().equals(memberId)) {
            System.err.println("❌ Utilisateur " + memberId + " ne peut pas supprimer le message " + messageId + " (appartient à " + message.getMemberId() + ")");
            return false;
        }

        // Vérifier que le message peut être supprimé
        if (!message.canBeDeleted()) {
            System.err.println("❌ Message " + messageId + " ne peut pas être supprimé (déjà supprimé)");
            return false;
        }

        try {
            // Marquer comme supprimé par l'auteur
            message.markAsDeletedByAuthor();

            Message updatedMessage = messageRepository.update(message);

            if (updatedMessage != null) {
                System.out.println("✅ Message supprimé par l'auteur - ID: " + messageId);
                return true;
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du message " + messageId + ":");
            e.printStackTrace();
        }

        return false;
    }
}