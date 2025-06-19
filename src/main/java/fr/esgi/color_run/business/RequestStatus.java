package fr.esgi.color_run.business;

import lombok.Getter;

@Getter
public enum RequestStatus {
    PENDING("En attente"),
    APPROVED("Approuvée"),
    REJECTED("Refusée");

    private final String displayName;

    RequestStatus(String displayName) {
        this.displayName = displayName;
    }

}