package fr.esgi.color_run.business;

import lombok.Data;

/**
 * Représente un utilisateur sur l'app
 */
@Data
public class Member {
    private Long id;
    private Role role;
    private String name;
    private String firstname;
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String city;
    private Integer zipCode;
    private double positionLatitude;
    private double positionLongitude;

    private static Long compteur = 0L;
    /**
     * Constructeur par défaut de la classe User.
     * Initialise l'identifiant, le role à coureur et les coordonnées à celle de Paris.
     */
    public Member() {
        this.id = compteur++;
        this.role = Role.RUNNER;
        this.positionLatitude = 48.8566;
        this.positionLongitude = 2.3522;
    }
}