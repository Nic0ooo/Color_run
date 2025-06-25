package fr.esgi.color_run.business;

public enum RequestType {
    BECOME_ORGANIZER("Devenir organisateur"),
    JOIN_ASSOCIATION("Rejoindre une association"),
    CREATE_ASSOCIATION("Créer une association");

    private final String displayName;

    RequestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}