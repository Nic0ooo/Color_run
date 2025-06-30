package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.business.Role;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.service.Course_memberService;
import fr.esgi.color_run.service.impl.Course_memberServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.*;

@WebServlet("/course-participants")
public class CourseParticipantsServlet extends HttpServlet {

    private Course_memberService courseMemberService;
    private static final int PARTICIPANTS_PER_PAGE = 10;

    @Override
    public void init() throws ServletException {
        super.init();
        this.courseMemberService = new Course_memberServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Vérification session
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Member member = (Member) session.getAttribute("member");
        if (member == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Vérification permissions (ADMIN ou ORGANIZER uniquement)
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ORGANIZER) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès refusé");
            return;
        }

        // Récupération courseId
        String courseIdParam = req.getParameter("courseId");
        if (courseIdParam == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "courseId manquant");
            return;
        }

        try {
            Long courseId = Long.parseLong(courseIdParam);

            // Récupération page (défaut = 1)
            int page = 1;
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            }

            // Récupération des participants avec pagination
            Map<Course_member, Member> allParticipants = getParticipantsWithDetails(courseId);
            List<Map.Entry<Course_member, Member>> participantsList = new ArrayList<>(allParticipants.entrySet());

            // Calculs pagination
            int totalParticipants = participantsList.size();
            int totalPages = (int) Math.ceil((double) totalParticipants / PARTICIPANTS_PER_PAGE);
            int startIndex = (page - 1) * PARTICIPANTS_PER_PAGE;
            int endIndex = Math.min(startIndex + PARTICIPANTS_PER_PAGE, totalParticipants);

            // Sous-liste pour la page actuelle
            List<Map.Entry<Course_member, Member>> currentPageParticipants =
                    participantsList.subList(startIndex, endIndex);

            // Configuration Thymeleaf
            TemplateEngine templateEngine = ThymeleafConfiguration.getTemplateEngine();
            WebContext context = new WebContext(
                    ThymeleafConfiguration.getApplication().buildExchange(req, resp));

            // Variables pour le template
            context.setVariable("courseId", courseId);
            context.setVariable("member", member);
            context.setVariable("participants", currentPageParticipants);
            context.setVariable("currentPage", page);
            context.setVariable("totalPages", totalPages);
            context.setVariable("totalParticipants", totalParticipants);
            context.setVariable("hasParticipants", totalParticipants > 0);

            // Rendu du fragment
            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            templateEngine.process("fragments/participants", context, resp.getWriter());

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paramètres invalides");
        } catch (Exception e) {
            System.err.println("❌ Erreur CourseParticipantsServlet : " + e.getMessage());
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur serveur");
        }
    }

    /**
     * Récupère les participants avec leurs détails
     */
    private Map<Course_member, Member> getParticipantsWithDetails(Long courseId) {
        Map<Course_member, Member> participantsWithDetails = new LinkedHashMap<>();

        try {
            List<Member> members = courseMemberService.findMembersByCourseId(courseId);

            for (Member member : members) {
                Optional<Course_member> registrationOpt =
                        courseMemberService.getRegistrationDetails(courseId, member.getId());

                if (registrationOpt.isPresent()) {
                    participantsWithDetails.put(registrationOpt.get(), member);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération participants : " + e.getMessage());
        }

        return participantsWithDetails;
    }
}