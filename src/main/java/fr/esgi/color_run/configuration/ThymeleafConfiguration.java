package fr.esgi.color_run.configuration;

import jakarta.servlet.ServletContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public class ThymeleafConfiguration {

    private static TemplateEngine templateEngine;
    private static JakartaServletWebApplication application;

    public static void initialize(ServletContext servletContext) {
        // Initialisation de l'application web Thymeleaf
        application = JakartaServletWebApplication.buildApplication(servletContext);

        // Initialisation du résolveur de templates
        WebApplicationTemplateResolver templateResolver =
                new WebApplicationTemplateResolver(application);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false); // Désactiver le cache en développement
        templateResolver.setCharacterEncoding("UTF-8");

        // Initialisation du moteur de templates
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public static JakartaServletWebApplication getApplication() {
        return application;
    }
}