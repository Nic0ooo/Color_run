package fr.esgi.color_run.business;

import lombok.Data;

import javax.print.attribute.DateTimeSyntax;
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
    private String startDate;
    private String endDate;
    private double startpositionLatitude;
    private double startpositionLongitude;
    private double endpositionLatitude;
    private double endpositionLongitude;
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

    public String getMonthName() {
        // Si startDate est au format "yyyy-MM-dd" ou similaire
        if (startDate != null && startDate.length() >= 7) {
            String monthNumber = startDate.substring(5, 7); // Extrait "MM" de "yyyy-MM-dd"
            switch (monthNumber) {
                case "01": return "january";
                case "02": return "february";
                case "03": return "march";
                case "04": return "april";
                case "05": return "may";
                case "06": return "june";
                case "07": return "july";
                case "08": return "august";
                case "09": return "september";
                case "10": return "october";
                case "11": return "november";
                case "12": return "december";
                default: return "";
            }
        }
        return "";
    }
}
