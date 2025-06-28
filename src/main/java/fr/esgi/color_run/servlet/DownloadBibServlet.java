package fr.esgi.color_run.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
// Ajoutez ces imports en haut de votre SimpleBibServlet.java

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Course_member;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.Course_memberRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.repository.impl.Course_memberRepositoryImpl;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.Course_memberService;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import fr.esgi.color_run.service.impl.Course_memberServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@WebServlet(urlPatterns = {"/generate-bib", "/download-bib"})
public class DownloadBibServlet extends HttpServlet {

    private CourseService courseService;
    private Course_memberService courseMemberService;
    private ObjectMapper objectMapper;
    private Random random;

    @Override
    public void init() throws ServletException {
        super.init();

        // ✅ Initialiser sans GeocodingService pour CourseRepositoryImpl
        CourseRepository courseRepository = new CourseRepositoryImpl(null); // null pour GeocodingService
        Course_memberRepository courseMemberRepository = new Course_memberRepositoryImpl();

        // ✅ Initialiser CourseService avec les bons paramètres
        this.courseService = new CourseServiceImpl(courseRepository, null); // null pour GeocodingService
        this.courseMemberService = new Course_memberServiceImpl(courseMemberRepository);
        this.objectMapper = new ObjectMapper();
        this.random = new Random();

        System.out.println("SimpleBibServlet initialisé avec repositories corrects et support PDF");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String servletPath = req.getServletPath();
        System.out.println("SimpleBibServlet POST - Path: " + servletPath);

        // Vérification de la session
        Member member = getCurrentMember(req);
        if (member == null) {
            sendErrorResponse(resp, 401, "Vous devez être connecté");
            return;
        }

        try {
            if ("/generate-bib".equals(servletPath)) {
                handleGenerateBib(req, resp, member);
            } else {
                sendErrorResponse(resp, 404, "Endpoint non trouvé");
            }
        } catch (Exception e) {
            System.err.println("Erreur dans SimpleBibServlet POST:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur interne du serveur");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String servletPath = req.getServletPath();
        System.out.println("SimpleBibServlet GET - Path: " + servletPath);

        // Vérification de la session
        Member member = getCurrentMember(req);
        if (member == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            if ("/download-bib".equals(servletPath)) {
                handleDownloadBibPDF(req, resp, member); // ✅ Changé pour PDF
            } else {
                resp.sendError(404, "Page non trouvée");
            }
        } catch (Exception e) {
            System.err.println("Erreur dans SimpleBibServlet GET:");
            e.printStackTrace();
            resp.sendError(500, "Erreur interne du serveur: " + e.getMessage());
        }
    }

    /**
     * Génère un dossard simple
     */
    private void handleGenerateBib(HttpServletRequest req, HttpServletResponse resp, Member member)
            throws IOException {

        String courseIdStr = req.getParameter("courseId");

        if (courseIdStr == null || courseIdStr.trim().isEmpty()) {
            sendErrorResponse(resp, 400, "Course ID manquant");
            return;
        }

        try {
            Long courseId = Long.parseLong(courseIdStr);

            System.out.println("Génération dossard - Course: " + courseId + ", Member: " + member.getId());

            // Vérifier que le membre est inscrit et a payé
            if (!courseMemberService.isMemberRegisteredAndPaid(courseId, member.getId())) {
                sendErrorResponse(resp, 403, "Vous devez être inscrit et avoir payé pour générer un dossard");
                return;
            }

            // Récupérer l'inscription
            Optional<Course_member> registrationOpt = courseMemberService.getRegistrationDetails(courseId, member.getId());
            if (!registrationOpt.isPresent()) {
                sendErrorResponse(resp, 404, "Inscription non trouvée");
                return;
            }

            Course_member registration = registrationOpt.get();

            // Vérifier qu'un dossard n'existe pas déjà
            if (registration.getBibNumber() != null && !registration.getBibNumber().isEmpty()) {
                sendErrorResponse(resp, 409, "Un dossard existe déjà : " + registration.getBibNumber());
                return;
            }

            // Générer un numéro de dossard unique
            String bibNumber = generateUniqueBibNumber();

            // Mettre à jour l'inscription avec le numéro de dossard
            registration.setBibNumber(bibNumber);
            courseMemberService.save(registration);

            System.out.println("Dossard généré avec succès: " + bibNumber);

            // Réponse de succès
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dossard généré avec succès");
            response.put("bibNumber", bibNumber);

            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(objectMapper.writeValueAsString(response));

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, 400, "Course ID invalide");
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du dossard:");
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Erreur lors de la génération du dossard");
        }
    }

    /**
     * ✅ NOUVEAU : Télécharge le dossard en PDF
     */
    private void handleDownloadBibPDF(HttpServletRequest req, HttpServletResponse resp, Member member)
            throws IOException {

        String courseIdStr = req.getParameter("courseId");

        if (courseIdStr == null || courseIdStr.trim().isEmpty()) {
            resp.sendError(400, "Course ID manquant");
            return;
        }

        try {
            Long courseId = Long.parseLong(courseIdStr);

            System.out.println("Téléchargement dossard PDF - Course: " + courseId + ", Member: " + member.getId());

            // Vérifier que le membre est inscrit et a payé
            if (!courseMemberService.isMemberRegisteredAndPaid(courseId, member.getId())) {
                resp.sendError(403, "Vous devez être inscrit et avoir payé");
                return;
            }

            // Récupérer l'inscription
            Optional<Course_member> registrationOpt = courseMemberService.getRegistrationDetails(courseId, member.getId());
            if (!registrationOpt.isPresent()) {
                resp.sendError(404, "Inscription non trouvée");
                return;
            }

            Course_member registration = registrationOpt.get();

            // Récupérer les infos de la course
            Course course = courseService.getCourseById(courseId);
            if (course == null) {
                resp.sendError(404, "Course non trouvée");
                return;
            }

            // ✅ Utiliser l'ID de courseMember comme numéro de coureur
            String runnerNumber = String.valueOf(registration.getId());

            // ✅ Générer le PDF simple
            byte[] pdfBytes = generateSimpleStandardBib(member, course, runnerNumber);

            // Configurer la réponse pour le téléchargement PDF
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"dossard_" + runnerNumber + "_" +
                            course.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf\"");
            resp.setContentLength(pdfBytes.length);

            // Envoyer le PDF
            resp.getOutputStream().write(pdfBytes);
            resp.getOutputStream().flush();

            System.out.println("✅ Dossard PDF envoyé - Coureur n°" + runnerNumber);

        } catch (NumberFormatException e) {
            resp.sendError(400, "Course ID invalide");
        } catch (Exception e) {
            System.err.println("Erreur lors du téléchargement du dossard PDF:");
            e.printStackTrace();
            resp.sendError(500, "Erreur lors du téléchargement: " + e.getMessage());
        }
    }

    /**
     * ✅ NOUVEAU : Génère un dossard simple avec QR code
     */
    private byte[] generateSimpleStandardBib(Member member, Course course, String runnerNumber) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(50, 50, 50, 50); // Marges normales

            // Polices
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Couleurs
            DeviceRgb primaryColor = new DeviceRgb(20, 184, 166);    // Teal
            DeviceRgb darkColor = new DeviceRgb(31, 41, 55);        // Gris foncé

            // === ENCART COLORÉ HEADER ===
            Table headerTable = new Table(1);
            headerTable.setWidth(UnitValue.createPercentValue(100));

            Cell headerCell = new Cell()
                    .add(new Paragraph(course.getName().toUpperCase())
                            .setFont(titleFont)
                            .setFontSize(24)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(5))
                    .add(new Paragraph("DOSSARD OFFICIEL")
                            .setFont(regularFont)
                            .setFontSize(14)
                            .setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(primaryColor)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(20);

            headerTable.addCell(headerCell);
            document.add(headerTable);

            // Espacement modéré
            document.add(new Paragraph("\n\n"));

            // === NUMÉRO DE COUREUR CENTRÉ ===
            Table numberTable = new Table(1);
            numberTable.setWidth(UnitValue.createPercentValue(100));

            Cell numberCell = new Cell()
                    .add(new Paragraph("N°")
                            .setFont(regularFont)
                            .setFontSize(18)
                            .setFontColor(new DeviceRgb(120, 120, 120))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(12))
                    .add(new Paragraph(runnerNumber)
                            .setFont(titleFont)
                            .setFontSize(130)  // Taille intermédiaire
                            .setFontColor(darkColor)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(12))
                    .setBorder(new SolidBorder(primaryColor, 4))
                    .setBackgroundColor(new DeviceRgb(248, 250, 252))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(35); // Padding modéré

            numberTable.addCell(numberCell);
            document.add(numberTable);

            // Espacement modéré
            document.add(new Paragraph("\n\n"));

            // === QR CODE ===
            document.add(new Paragraph("\n"));

            // Générer le QR code
            String qrData = "RUNNER:" + runnerNumber + "|COURSE:" + course.getId() + "|MEMBER:" + member.getId();
            com.itextpdf.barcodes.BarcodeQRCode qrcode = new com.itextpdf.barcodes.BarcodeQRCode(qrData);
            com.itextpdf.layout.element.Image qrCodeImage = new com.itextpdf.layout.element.Image(qrcode.createFormXObject(pdfDoc));

            // Taille et position du QR code
            qrCodeImage.setWidth(80);
            qrCodeImage.setHeight(80);
            qrCodeImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

            document.add(qrCodeImage);

            // Espace en bas modéré
            document.add(new Paragraph("\n\n"));

            document.close();

        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Erreur lors de la génération du PDF", e);
        }

        return baos.toByteArray();
    }

    /**
     * Génère un numéro de dossard unique simple
     */
    private String generateUniqueBibNumber() {
        // Format simple: 4 chiffres
        int number = random.nextInt(9000) + 1000; // Entre 1000 et 9999
        return String.valueOf(number);
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
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);

        resp.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        System.err.println("Erreur " + statusCode + ": " + message);
    }

    @Override
    public void destroy() {
        System.out.println("SimpleBibServlet détruit");
        super.destroy();
    }
}