package fr.esgi.color_run.business;

import lombok.Getter;

@Getter
public enum Role {
    RUNNER("Participant"),
    ORGANIZER("Organisateur"),
    ADMIN("Administrateur");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }



}