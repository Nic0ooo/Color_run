package fr.esgi.color_run.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.esgi.color_run.business.Discussion;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Message;
import fr.esgi.color_run.repository.MessageRepository;
import fr.esgi.color_run.repository.impl.MessageRepositoryImpl;
import fr.esgi.color_run.service.DiscussionService;
import fr.esgi.color_run.service.MessageService;
import fr.esgi.color_run.service.impl.DiscussionServiceImpl;
import fr.esgi.color_run.service.impl.MessageServiceImpl;
import fr.esgi.color_run.util.MessageFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(urlPatterns = {"/chat/messages", "/chat/send", "/chat/moderate", "/chat/update", "/chat/delete-own"})
public class ChatServlet extends HttpServlet {

    private MessageService messageService;
    private MessageRepository messageRepository;
    private DiscussionService discussionService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        this.messageService = new MessageServiceImpl();
        this.messageRepository = new MessageRepositoryImpl();
        this.discussionService = new DiscussionServiceImpl();
        this.objectMapper = new ObjectMapper();
        System.out.println("ChatServlet initialisé avec tous les services");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String servletPath = req.getServletPath();
        System.out.println("ChatServlet GET - Path: " + servletPath);

        // Configuration de la réponse JSON
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");

        try {
            // Vérification de la session
            Member member = getCurrentMember(req);
            if (member == null) {
                sendErrorResponse(resp, 401, "Vous devez être connecté pour accéder au chat");
                return;
            }

            if ("/chat/messages".equals(servletPath)) {
                handleGetMessages(req, resp, member);
            } else {
                sendErrorResponse(resp, 404, "Endpoint non trouvé");
            }

        } catch (Exception e) {
            System.err.println("Erreur dans ChatServlet GET:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur interne du serveur");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String servletPath = req.getServletPath();
        System.out.println("ChatServlet POST - Path: " + servletPath);

        // Configuration de la réponse JSON
        resp.setContentType("application/json;charset=UTF-8");

        try {
            // Vérification de la session
            Member member = getCurrentMember(req);
            if (member == null) {
                sendErrorResponse(resp, 401, "Vous devez être connecté pour cette action");
                return;
            }

            switch (servletPath) {
                case "/chat/send":
                    handleSendMessage(req, resp, member);
                    break;
                case "/chat/moderate":
                    handleModeration(req, resp, member);
                    break;
                case "/chat/update":
                    handleUpdateMessage(req, resp, member);
                    break;
                case "/chat/delete-own":
                    handleDeleteOwnMessage(req, resp, member);
                    break;
                default:
                    sendErrorResponse(resp, 404, "Endpoint non trouvé");
            }

        } catch (Exception e) {
            System.err.println("Erreur dans ChatServlet POST:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur interne du serveur");
        }
    }

    /**
     * Gestion de la récupération des messages
     */
    private void handleGetMessages(HttpServletRequest req, HttpServletResponse resp, Member member)
            throws IOException {

        // Récupération des paramètres
        String courseIdStr = req.getParameter("courseId");
        String lastMessageIdStr = req.getParameter("lastMessageId");

        if (courseIdStr == null || courseIdStr.trim().isEmpty()) {
            sendErrorResponse(resp, 400, "Course ID manquant");
            return;
        }

        try {
            Long courseId = Long.parseLong(courseIdStr);
            Long lastMessageId = 0L;

            if (lastMessageIdStr != null && !lastMessageIdStr.trim().isEmpty()) {
                lastMessageId = Long.parseLong(lastMessageIdStr);
            }

            System.out.println("Récupération messages - Course: " + courseId +
                    ", Member: " + member.getId() + " (" + member.getRole() + ")" +
                    ", Since: " + lastMessageId);

            // Vérification des autorisations
            if (!messageService.canAccessChat(courseId, member.getId())) {
                sendErrorResponse(resp, 403, "Accès non autorisé au chat de cette course");
                return;
            }

            // Récupération des messages
            List<Message> messages;
            if (lastMessageId > 0) {
                messages = messageService.getNewMessages(courseId, member.getId(), lastMessageId);
            } else {
                messages = messageService.getCourseMessages(courseId, member.getId());
            }

            // Vérification des droits de modération
            boolean isCurrentUserModerator = isUserModerator(member, courseId);

            System.out.println("🛡️ Droits de modération pour course " + courseId +
                    " et member " + member.getId() + " (role: " + member.getRole() + "): " + isCurrentUserModerator);

            // Préparation de la réponse avec informations utilisateur
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", convertMessagesToJson(messages, member, isCurrentUserModerator));
            response.put("count", messages.size());
            response.put("currentUserId", member.getId());
            response.put("currentUserRole", member.getRole().name());
            response.put("isCurrentUserModerator", isCurrentUserModerator);

            // Envoi de la réponse
            resp.getWriter().write(objectMapper.writeValueAsString(response));

            System.out.println(messages.size() + " messages envoyés - Modérateur: " + isCurrentUserModerator);

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, 400, "Paramètres invalides");
        }
    }

    /**
     * Vérification si utilisateur est modérateur
     */
    private boolean isUserModerator(Member member, Long courseId) {
        if (member == null) {
            return false;
        }

        // Les admins et organisateurs sont modérateurs de toutes les courses
        return member.getRole().name().equals("ADMIN") || member.getRole().name().equals("ORGANIZER");
    }

    /**
     * Gestion de l'envoi de messages
     */
    private void handleSendMessage(HttpServletRequest req, HttpServletResponse resp, Member member)
            throws IOException {

        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        if (jsonBody.length() == 0) {
            sendErrorResponse(resp, 400, "Corps de la requête vide");
            return;
        }

        try {
            // Parse du JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(jsonBody.toString(), Map.class);

            Object courseIdObj = requestData.get("courseId");
            Object contentObj = requestData.get("content");

            if (courseIdObj == null || contentObj == null) {
                sendErrorResponse(resp, 400, "Course ID ou contenu manquant");
                return;
            }

            Long courseId = Long.parseLong(courseIdObj.toString());
            String content = contentObj.toString().trim();

            System.out.println("💬 Envoi message - Course: " + courseId +
                    ", Member: " + member.getId() + ", Content: " + content.substring(0, Math.min(content.length(), 50)));

            // Validation du contenu
            if (content.isEmpty()) {
                sendErrorResponse(resp, 400, "Le message ne peut pas être vide");
                return;
            }

            if (content.length() > 1000) {
                sendErrorResponse(resp, 400, "Le message est trop long (maximum 1000 caractères)");
                return;
            }

            // Envoi du message
            Message sentMessage = messageService.sendMessage(courseId, member.getId(), content);

            if (sentMessage == null) {
                sendErrorResponse(resp, 403, "Impossible d'envoyer le message. Vérifiez que vous êtes inscrit et avez payé votre participation.");
                return;
            }

            // Réponse de succès
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Message envoyé avec succès");
            response.put("messageId", sentMessage.getId());

            resp.getWriter().write(objectMapper.writeValueAsString(response));

            System.out.println("Message envoyé avec succès - ID: " + sentMessage.getId());

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, 400, "Course ID invalide");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur lors de l'envoi du message");
        }
    }

    /**
     * Gestion de la modération (pin, masquer, supprimer)
     */
    private void handleModeration(HttpServletRequest req, HttpServletResponse resp, Member member)
            throws IOException {

        // Lecture du JSON depuis le body
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        if (jsonBody.length() == 0) {
            sendErrorResponse(resp, 400, "Corps de la requête vide");
            return;
        }

        try {
            // Parse du JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(jsonBody.toString(), Map.class);

            Object messageIdObj = requestData.get("messageId");
            Object actionObj = requestData.get("action");

            if (messageIdObj == null || actionObj == null) {
                sendErrorResponse(resp, 400, "Message ID ou action manquant");
                return;
            }

            Long messageId = Long.parseLong(messageIdObj.toString());
            String action = actionObj.toString();

            System.out.println("🛡️ Action de modération - Message: " + messageId +
                    ", Action: " + action + ", Modérateur: " + member.getId() + " (role: " + member.getRole() + ")");

            // VÉRIFICATION DES DROITS DE MODÉRATION
            Long courseId = getCourseIdFromMessageId(messageId);
            if (courseId == null) {
                sendErrorResponse(resp, 404, "Message ou course introuvable");
                return;
            }

            if (!isUserModerator(member, courseId)) {
                sendErrorResponse(resp, 403, "Vous n'avez pas les droits de modération pour cette course");
                return;
            }

            boolean success = false;
            String responseMessage = "";

            switch (action.toLowerCase()) {
                case "pin":
                case "toggle_pin":
                    success = messageService.togglePinMessage(messageId, member.getId());
                    responseMessage = success ? "Message épinglé/désépinglé" : "Impossible d'épingler/désépingler le message";
                    break;

                case "hide":
                    success = messageService.hideMessage(messageId, member.getId());
                    responseMessage = success ? "Message masqué" : "Impossible de masquer le message";
                    break;

                case "delete":
                    success = messageService.deleteMessage(messageId, member.getId());
                    responseMessage = success ? "Message supprimé" : "Impossible de supprimer le message";
                    break;

                default:
                    sendErrorResponse(resp, 400, "Action de modération inconnue: " + action);
                    return;
            }

            // Réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", responseMessage);
            response.put("action", action);
            response.put("messageId", messageId);

            if (!success) {
                resp.setStatus(403);
            }

            resp.getWriter().write(objectMapper.writeValueAsString(response));

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, 400, "Message ID invalide");
        } catch (Exception e) {
            System.err.println("Erreur lors de la modération:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur lors de la modération");
        }
    }

    /**
     * Récupération de courseId depuis un messageId
     */
    private Long getCourseIdFromMessageId(Long messageId) {
        try {
            Optional<Message> messageOpt = messageRepository.findById(messageId);
            if (messageOpt.isPresent()) {
                Message message = messageOpt.get();
                Optional<Discussion> discussionOpt = discussionService.getDiscussionById(message.getDiscussionId());
                if (discussionOpt.isPresent()) {
                    return discussionOpt.get().getCourseId();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du courseId:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gestion de la modification de message
     */
    private void handleUpdateMessage(HttpServletRequest req, HttpServletResponse resp, Member member)
            throws IOException {

        // Lecture du JSON depuis le body
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        if (jsonBody.length() == 0) {
            sendErrorResponse(resp, 400, "Corps de la requête vide");
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(jsonBody.toString(), Map.class);

            Object messageIdObj = requestData.get("messageId");
            Object contentObj = requestData.get("content");

            if (messageIdObj == null || contentObj == null) {
                sendErrorResponse(resp, 400, "Message ID ou contenu manquant");
                return;
            }

            Long messageId = Long.parseLong(messageIdObj.toString());
            String newContent = contentObj.toString().trim();

            System.out.println("Modification message - ID: " + messageId +
                    ", Member: " + member.getId() + ", Nouveau contenu: " +
                    newContent.substring(0, Math.min(newContent.length(), 50)));

            // Validation du contenu
            if (newContent.isEmpty()) {
                sendErrorResponse(resp, 400, "Le message ne peut pas être vide");
                return;
            }

            if (newContent.length() > 1000) {
                sendErrorResponse(resp, 400, "Le message est trop long (maximum 1000 caractères)");
                return;
            }

            // Utiliser la méthode du service pour la mise à jour
            MessageServiceImpl messageServiceImpl = (MessageServiceImpl) messageService;
            Message updatedMessage = messageServiceImpl.updateMessage(messageId, member.getId(), newContent);

            if (updatedMessage != null) {
                System.out.println("Message modifié avec succès - ID: " + messageId);

                // Réponse de succès
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Message modifié avec succès");
                response.put("messageId", messageId);

                resp.getWriter().write(objectMapper.writeValueAsString(response));
            } else {
                sendErrorResponse(resp, 403, "Impossible de modifier le message");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, 400, "Message ID invalide");
        } catch (Exception e) {
            System.err.println("Erreur lors de la modification du message:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur lors de la modification du message");
        }
    }

    /**
     * Gestion de la suppression de son propre message
     */
    private void handleDeleteOwnMessage(HttpServletRequest req, HttpServletResponse resp, Member member)
            throws IOException {

        // Lecture du JSON depuis le body
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        if (jsonBody.length() == 0) {
            sendErrorResponse(resp, 400, "Corps de la requête vide");
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(jsonBody.toString(), Map.class);

            Object messageIdObj = requestData.get("messageId");

            if (messageIdObj == null) {
                sendErrorResponse(resp, 400, "Message ID manquant");
                return;
            }

            Long messageId = Long.parseLong(messageIdObj.toString());

            System.out.println("🗑️ Suppression message - ID: " + messageId + ", Member: " + member.getId());

            // Utiliser la méthode du service pour la suppression
            MessageServiceImpl messageServiceImpl = (MessageServiceImpl) messageService;
            boolean success = messageServiceImpl.deleteOwnMessage(messageId, member.getId());

            if (success) {
                System.out.println("Message supprimé par l'auteur - ID: " + messageId);

                // Réponse de succès
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Message supprimé avec succès");
                response.put("messageId", messageId);

                resp.getWriter().write(objectMapper.writeValueAsString(response));
            } else {
                sendErrorResponse(resp, 403, "Impossible de supprimer le message");
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, 400, "Message ID invalide");
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du message:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur lors de la suppression du message");
        }
    }

    /**
     * Récupère le membre actuel depuis la session
     */
    private Member getCurrentMember(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }

        return (Member) session.getAttribute("member");
    }

    /**
     * Envoie une réponse d'erreur en JSON
     */
    private void sendErrorResponse(HttpServletResponse resp, int statusCode, String message)
            throws IOException {

        resp.setStatus(statusCode);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);

        resp.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        System.err.println("Erreur " + statusCode + ": " + message);
    }

    /**
     * Convertit la liste de messages en format JSON approprié pour le front-end
     */
    private List<Map<String, Object>> convertMessagesToJson(List<Message> messages, Member currentMember, boolean isCurrentUserModerator) {
        return messages.stream().map(message -> {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("discussionId", message.getDiscussionId());
            messageMap.put("memberId", message.getMemberId());
            messageMap.put("memberName", MessageFormatter.formatMemberName(message));
            messageMap.put("content", message.getContent());
            messageMap.put("originalContent", message.getOriginalContent());
            messageMap.put("date", message.getIsoDate());
            messageMap.put("formattedDate", message.getFormattedDate());
            messageMap.put("lastModifiedDate", message.getFormattedLastModifiedDate());
            messageMap.put("isPin", message.isPin());
            messageMap.put("isHidden", message.isHidden());
            messageMap.put("isModified", message.isModified());
            messageMap.put("isDeleted", message.isDeleted());
            messageMap.put("hiddenByMemberId", message.getHiddenByMemberId());

            // Ajout des informations sur les droits
            messageMap.put("isOwnMessage", message.getMemberId().equals(currentMember.getId()));
            messageMap.put("canEdit", message.getMemberId().equals(currentMember.getId()) && !message.isDeleted() && !message.isHidden());
            messageMap.put("canDelete", message.getMemberId().equals(currentMember.getId()) && !message.isDeleted());
            messageMap.put("canModerate", isCurrentUserModerator);

            System.out.println("📨 Message " + message.getId() + " - isModerator: " + isCurrentUserModerator +
                    " - Role: " + currentMember.getRole() + " - isDeleted: " + message.isDeleted() +
                    " - isHidden: " + message.isHidden());

            return messageMap;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void destroy() {
        System.out.println("🧹 ChatServlet détruit");
        super.destroy();
    }
}