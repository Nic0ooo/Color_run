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
    private Integer userCreatorId;
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
        this.userCreatorId = 0;
        this.startpositionLatitude = 48.8566;
        this.startpositionLongitude = 2.3522;
        this.endpositionLatitude = 49.8566;
        this.endpositionLongitude = 2.9522;
        this.maxOfRunners = 100;
        this.currentNumberOfRunners = 0;
        this.price = 0;
    }
}
