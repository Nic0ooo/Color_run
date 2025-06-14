package fr.esgi.color_run.business;

import lombok.Data;

import javax.print.attribute.DateTimeSyntax;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Représente une association sur l'app
 */
@Data
public class Course {
    private Long id;
    private String name;
    private String description;
    private Integer associationId;
    private Integer memberCreatorId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private double startpositionLatitude;
    private double startpositionLongitude;
    private double endpositionLatitude;
    private double endpositionLongitude;
    private Double distance;
    private String address;
    private String city;
    private Integer zipCode;
    private Integer maxOfRunners;
    private Integer currentNumberOfRunners;
    private double price;

    private static Long compteur = 0L;

    public Course() {
        this.id = compteur++;
        this.associationId = 0;
        this.memberCreatorId = 0;
        this.startpositionLatitude = 48.8566;
        this.startpositionLongitude = 2.3522;
        this.endpositionLatitude = 49.8566;
        this.endpositionLongitude = 2.9522;
        this.maxOfRunners = 100;
        this.currentNumberOfRunners = 0;
        this.price = 0;
    }

    public String getFormattedStartDate() {
        if (startDate == null) return "";
        return startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
    }

    public String getFormattedEndDate() {
        if (endDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH'h'mm");
        return endDate.format(formatter);
    }

    // Méthode pour afficher uniquement la date
    public String getFormattedDate() {
        if (startDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return startDate.format(formatter);
    }

    // Méthode pour afficher uniquement l'heure
    public String getFormattedTime() {
        if (startDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH'h'mm");
        return startDate.format(formatter);
    }

    public String getMonthName() {
        if (startDate != null) {
            int monthValue = startDate.getMonthValue(); // Récupère le numéro du mois (1-12)
            switch (monthValue) {
                case 1: return "january";
                case 2: return "february";
                case 3: return "march";
                case 4: return "april";
                case 5: return "may";
                case 6: return "june";
                case 7: return "july";
                case 8: return "august";
                case 9: return "september";
                case 10: return "october";
                case 11: return "november";
                case 12: return "december";
                default: return "";
            }
        }
        return "";
    }
}
