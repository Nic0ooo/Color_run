package fr.esgi.color_run.servlet;

import fr.esgi.color_run.business.Course;
import fr.esgi.color_run.business.Member;
import fr.esgi.color_run.configuration.ThymeleafConfiguration;
import fr.esgi.color_run.repository.CourseRepository;
import fr.esgi.color_run.repository.impl.CourseRepositoryImpl;
import fr.esgi.color_run.service.CourseSearchStrategy;
import fr.esgi.color_run.service.CourseService;
import fr.esgi.color_run.service.GeocodingService;
import fr.esgi.color_run.service.impl.CourseServiceImpl;
import fr.esgi.color_run.service.impl.GeocodingServiceImpl;
import fr.esgi.color_run.service.impl.search.CourseSearchStrategyFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//
//@WebServlet("/")
//public class HomeServlet extends HttpServlet {
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        TemplateEngine engine = ThymeleafConfiguration.getTemplateEngine();
//        WebContext context = new WebContext(
//                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
//        );
//
//        // Récupérer le membre de la session
//        Member member = (Member) req.getSession().getAttribute("member");
//        context.setVariable("member", member);
//
//        context.setVariable("pageTitle", "Accueil");
//
//        engine.process("home", context, resp.getWriter());
//    }
//
//
//}
@WebServlet("/")
public class HomeServlet extends HttpServlet {

    private TemplateEngine engine;
    private CourseService courseService;
    private CourseSearchStrategyFactory searchStrategyFactory;

    @Override
    public void init() throws ServletException {
        engine = ThymeleafConfiguration.getTemplateEngine();

        GeocodingService geocodingService = new GeocodingServiceImpl();

        CourseRepository courseRepository = new CourseRepositoryImpl(geocodingService);

        courseService = new CourseServiceImpl(courseRepository, geocodingService);

        searchStrategyFactory = new CourseSearchStrategyFactory(courseService, geocodingService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
        );

        // Récupérer le membre de la session
        Member member = (Member) req.getSession().getAttribute("member");
        context.setVariable("member", member);

        // Récupérer toutes les courses depuis le service
        List<Course> courses = courseService.listAllCourses();
        context.setVariable("courses", courses);

        context.setVariable("pageTitle", "Accueil");

        engine.process("home", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("POST request received");
        System.out.println("Parameters: " + req.getParameterMap());

        WebContext context = new WebContext(
                ThymeleafConfiguration.getApplication().buildExchange(req, resp)
        );

        // Récupérer les membres de la session
        Member member =  (Member) req.getSession().getAttribute("member");
        context.setVariable("member", member);

        // Avant de sélectionner la stratégie
        System.out.println("Selecting search strategy...");

        // Selectionner la stratégie de recherche appropriée
        CourseSearchStrategy searchStrategy = searchStrategyFactory.getStrategy(req);

        // Après avoir sélectionné la stratégie
        System.out.println("Selected strategy: " + searchStrategy.getClass().getSimpleName());

        // Récupérer toutes les courses depuis le service
        List<Course> courses = searchStrategy.search(req);
        context.setVariable("courses", courses);

        // Après avoir récupéré les courses
        System.out.println("Found " + courses.size() + " courses");

        // ajouter les paramètres de contexte spécifiques à la stratégie
        Map<String,  Object> contextParams = searchStrategy.getContextParameters(req);
        for (Map.Entry<String, Object> entry: contextParams.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }

        context.setVariable("pageTitle", "Resultats de la recherche");

        // Traiter la réponse
        engine.process("home", context, resp.getWriter());
    }

}
