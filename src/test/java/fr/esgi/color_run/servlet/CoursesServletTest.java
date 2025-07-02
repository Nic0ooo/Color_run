package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.*;
import fr.esgi.color_run.service.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests complets pour CoursesServlet")
class CoursesServletTest {

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private CourseService courseService;
    @Mock private Course_memberService courseMemberService;
    @Mock private MemberService memberService;
    @Mock private AssociationService associationService;
    @Mock private Association_memberService associationMemberService;

    private CoursesServlet coursesServlet;
    private Member testMember;
    private Member adminMember;
    private Course testCourse;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        coursesServlet = new CoursesServlet();
        testMember = createTestMember();
        adminMember = createAdminMember();
        testCourse = createTestCourse();

        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        injectServices();
    }

    // ========================
    // TESTS DOPOST (prioritaires car moins complexes)
    // ========================

    @Test
    @DisplayName("doPost - Création course avec succès")
    void doPost_ShouldCreateCourse_WhenValidData() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("create");
        setupValidCourseParameters();
        when(request.getContextPath()).thenReturn("/color-run");

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(courseService).createCourse(any(Course.class));
        verify(response).sendRedirect("/color-run/courses?success=course_created");
    }

    @Test
    @DisplayName("doPost - Échec création si coordonnées manquantes")
    void doPost_ShouldFailCreate_WhenMissingCoordinates() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("name")).thenReturn("Test Course");
        when(request.getParameter("startLatitude")).thenReturn(""); // Manquant
        when(request.getContextPath()).thenReturn("/color-run");

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(courseService, never()).createCourse(any(Course.class));
        verify(response).sendRedirect("/color-run/courses?error=missing_coordinates");
    }

    @Test
    @DisplayName("doPost - Échec création si coordonnées invalides")
    void doPost_ShouldFailCreate_WhenInvalidCoordinates() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("name")).thenReturn("Test Course");
        when(request.getParameter("startLatitude")).thenReturn("invalid");
        when(request.getParameter("startLongitude")).thenReturn("2.3522");
        when(request.getParameter("endLatitude")).thenReturn("48.8566");
        when(request.getParameter("endLongitude")).thenReturn("2.3522");
        when(request.getContextPath()).thenReturn("/color-run");

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(courseService, never()).createCourse(any(Course.class));
        verify(response).sendRedirect("/color-run/courses?error=invalid_coordinates");
    }

    @Test
    @DisplayName("doPost - Mise à jour course avec succès")
    void doPost_ShouldUpdateCourse_WhenValidData() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("courseId")).thenReturn("1");
        setupValidCourseParameters();
        when(request.getContextPath()).thenReturn("/color-run");
        when(courseService.getCourseById(1L)).thenReturn(testCourse);

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(courseService).getCourseById(1L);
        verify(courseService).updateCourse(any(Course.class));
        verify(response).sendRedirect("/color-run/courses?success=course_updated");
    }

    @Test
    @DisplayName("doPost - Échec mise à jour si course inexistante")
    void doPost_ShouldFailUpdate_WhenCourseNotFound() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("courseId")).thenReturn("999");
        setupValidCourseParameters(); // Ajouter tous les paramètres
        when(courseService.getCourseById(999L)).thenReturn(null);

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(courseService).getCourseById(999L);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Course non trouvée.");
        verify(courseService, never()).updateCourse(any(Course.class));
    }

    @Test
    @DisplayName("doPost - Erreur si action inconnue")
    void doPost_ShouldReturnError_WhenUnknownAction() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("unknown_action");

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Action non reconnue.");
    }

    @Test
    @DisplayName("doPost - Gestion exception lors création")
    void doPost_ShouldHandleException_WhenCreateFails() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("create");
        setupValidCourseParameters();
        when(request.getContextPath()).thenReturn("/color-run");
        doThrow(new RuntimeException("Database error")).when(courseService).createCourse(any(Course.class));

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(courseService).createCourse(any(Course.class));
        verify(response).sendRedirect("/color-run/courses?error=creation_failed");
    }

    @Test
    @DisplayName("doPost - Gestion exception lors mise à jour")
    void doPost_ShouldHandleException_WhenUpdateFails() throws ServletException, IOException {
        // Arrange
        when(request.getParameter("action")).thenReturn("update");
        when(request.getParameter("courseId")).thenReturn("1");
        setupValidCourseParameters();
        when(request.getContextPath()).thenReturn("/color-run");
        when(courseService.getCourseById(1L)).thenReturn(testCourse);
        doThrow(new RuntimeException("Database error")).when(courseService).updateCourse(any(Course.class));

        // Act
        coursesServlet.doPost(request, response);

        // Assert
        verify(courseService).updateCourse(any(Course.class));
        verify(response).sendRedirect("/color-run/courses?error=update_failed");
    }

    // ========================
    // TESTS DOGET - REQUÊTES AJAX (sans Thymeleaf)
    // ========================

    @Test
    @DisplayName("doGet - Requête AJAX avec filtrage")
    void doGet_ShouldHandleAjaxRequest_WithFiltering() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("member")).thenReturn(testMember);
        when(request.getParameter("ajax")).thenReturn("true");
        when(request.getParameter("search")).thenReturn("Paris");
        when(request.getParameter("courseFilter")).thenReturn("all");
        when(request.getParameter("upcomingPage")).thenReturn("1");
        when(request.getParameter("pastPage")).thenReturn("1");
        when(request.getParameter("pageSize")).thenReturn("6");

        doReturn(new ArrayList<>()).when(associationMemberService).getAssociationsByOrganizer(anyLong());
        when(courseService.searchAndSortCourses(eq("Paris"), any(), any(), any(), any(), eq(true)))
                .thenReturn(Arrays.asList(testCourse));
        when(courseService.searchAndSortCourses(eq("Paris"), any(), any(), any(), any(), eq(false)))
                .thenReturn(new ArrayList<>());
        when(courseMemberService.countRegisteredAndPaidMembers(anyLong())).thenReturn(10);

        // Act
        coursesServlet.doGet(request, response);

        // Assert
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(courseService).searchAndSortCourses(eq("Paris"), any(), any(), any(), any(), eq(true));
        verify(courseService).searchAndSortCourses(eq("Paris"), any(), any(), any(), any(), eq(false));

        String jsonResponse = responseWriter.toString();
        assertFalse(jsonResponse.isEmpty());
        assertTrue(jsonResponse.contains("upcomingCourses"));
        assertTrue(jsonResponse.contains("pastCourses"));
        assertTrue(jsonResponse.contains("upcomingPagination"));
    }

    @Test
    @DisplayName("doGet - AJAX avec filtre mes courses créées")
    void doGet_ShouldHandleAjax_MyCreatedCourses() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("member")).thenReturn(testMember);
        when(request.getParameter("ajax")).thenReturn("true");
        when(request.getParameter("courseFilter")).thenReturn("my-created");
        when(request.getParameter("upcomingPage")).thenReturn("1");
        when(request.getParameter("pastPage")).thenReturn("1");

        doReturn(new ArrayList<>()).when(associationMemberService).getAssociationsByOrganizer(anyLong());
        when(courseService.searchAndSortCoursesByCreator(any(), any(), any(), any(), any(), eq(true), eq(testMember.getId())))
                .thenReturn(Arrays.asList(testCourse));
        when(courseService.searchAndSortCoursesByCreator(any(), any(), any(), any(), any(), eq(false), eq(testMember.getId())))
                .thenReturn(new ArrayList<>());
        when(courseMemberService.countRegisteredAndPaidMembers(anyLong())).thenReturn(5);

        // Act
        coursesServlet.doGet(request, response);

        // Assert
        verify(courseService).searchAndSortCoursesByCreator(any(), any(), any(), any(), any(), eq(true), eq(testMember.getId()));
        verify(courseService).searchAndSortCoursesByCreator(any(), any(), any(), any(), any(), eq(false), eq(testMember.getId()));

        String jsonResponse = responseWriter.toString();
        assertTrue(jsonResponse.contains("upcomingCourses"));
    }

    @Test
    @DisplayName("doGet - AJAX avec filtre mes inscriptions")
    void doGet_ShouldHandleAjax_MyRegisteredCourses() throws Exception {
        // Arrange
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("member")).thenReturn(testMember);
        when(request.getParameter("ajax")).thenReturn("true");
        when(request.getParameter("courseFilter")).thenReturn("my-registered");
        when(request.getParameter("upcomingPage")).thenReturn("1");
        when(request.getParameter("pastPage")).thenReturn("1");

        doReturn(new ArrayList<>()).when(associationMemberService).getAssociationsByOrganizer(anyLong());
        when(courseMemberService.findUpcomingCoursesByMemberId(testMember.getId()))
                .thenReturn(Arrays.asList(testCourse));
        when(courseMemberService.findPastCoursesByMemberId(testMember.getId()))
                .thenReturn(new ArrayList<>());
        when(courseMemberService.countRegisteredAndPaidMembers(anyLong())).thenReturn(8);

        // Act
        coursesServlet.doGet(request, response);

        // Assert
        verify(courseMemberService).findUpcomingCoursesByMemberId(testMember.getId());
        verify(courseMemberService).findPastCoursesByMemberId(testMember.getId());
    }

    // ========================
    // TESTS D'INITIALISATION
    // ========================

    @Test
    @DisplayName("init - Initialisation du servlet")
    void init_ShouldInitializeServices() throws ServletException {
        // Arrange
        CoursesServlet servlet = new CoursesServlet();

        // Act & Assert
        assertDoesNotThrow(() -> servlet.init());
    }

    // ========================
    // MÉTHODES UTILITAIRES
    // ========================

    private Member createTestMember() {
        Member member = new Member();
        member.setId(1L);
        member.setEmail("test@example.com");
        member.setFirstname("John");
        member.setName("Doe");
        member.setRole(Role.RUNNER);
        return member;
    }

    private Member createAdminMember() {
        Member admin = new Member();
        admin.setId(10L);
        admin.setEmail("admin@example.com");
        admin.setFirstname("Admin");
        admin.setName("User");
        admin.setRole(Role.ADMIN);
        return admin;
    }

    private Course createTestCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setName("Test Course");
        course.setDescription("Description de test");
        course.setStartDate(LocalDateTime.now().plusDays(30));
        course.setCity("Paris");
        course.setAddress("123 rue Test");
        course.setDistance(5.0);
        course.setMaxOfRunners(100);
        course.setCurrentNumberOfRunners(50);
        course.setPrice(25.0);
        course.setZipCode(75001);
        course.setAssociationId(1);
        course.setMemberCreatorId(1);
        course.setStartpositionLatitude(48.8566);
        course.setStartpositionLongitude(2.3522);
        course.setEndpositionLatitude(48.8566);
        course.setEndpositionLongitude(2.3522);
        return course;
    }

    private void setupValidCourseParameters() {
        when(request.getParameter("name")).thenReturn("Test Course");
        when(request.getParameter("description")).thenReturn("Description");
        when(request.getParameter("city")).thenReturn("Paris");
        when(request.getParameter("address")).thenReturn("123 rue Test");
        when(request.getParameter("startDate")).thenReturn("2024-06-15T10:00");
        when(request.getParameter("endDate")).thenReturn("2024-06-15T12:00");
        when(request.getParameter("startLatitude")).thenReturn("48.8566");
        when(request.getParameter("startLongitude")).thenReturn("2.3522");
        when(request.getParameter("endLatitude")).thenReturn("48.8567");
        when(request.getParameter("endLongitude")).thenReturn("2.3523");
        when(request.getParameter("distance")).thenReturn("5.0");
        when(request.getParameter("zipCode")).thenReturn("75001");
        when(request.getParameter("maxOfRunners")).thenReturn("100");
        when(request.getParameter("associationId")).thenReturn("1");
        when(request.getParameter("memberCreatorId")).thenReturn("1");
        when(request.getParameter("price")).thenReturn("25.0");
    }

    private void injectServices() {
        setField(coursesServlet, "courseService", courseService);
        setField(coursesServlet, "courseMemberService", courseMemberService);
        setField(coursesServlet, "memberService", memberService);
        setField(coursesServlet, "associationService", associationService);
        setField(coursesServlet, "associationMemberService", associationMemberService);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            // Ignorer les erreurs d'injection
        }
    }
}