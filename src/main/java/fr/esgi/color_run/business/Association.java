package fr.esgi.color_run.business;

import lombok.Data;

/**
 * Représente une association sur l'app
 */
@Data
public class Association {
    private Long id;
    private String name;
    private String description;
    private String websiteLink;
    private String logoPath;
    private String email;
    private Integer phoneNumber;
    private String address;
    private String city;
    private Integer zipCode;

    private static Long compteur = 0L;
    /**
     * Constructeur par défaut de la classe Association.
     * Initialise l'identifiant et les coordonnées à celle de Paris.
     */
    public Association() {
        this.id = compteur++;
        this.websiteLink = "https://www.esgi.fr";
        this.logoPath = "/ressources/img/logo_esgi.png";
    }
}
