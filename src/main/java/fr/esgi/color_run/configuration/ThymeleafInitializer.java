package fr.esgi.color_run.configuration;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ThymeleafInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Initialiser la configuration Thymeleaf
        ThymeleafConfiguration.initialize(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nettoyage si nécessaire
    }
}