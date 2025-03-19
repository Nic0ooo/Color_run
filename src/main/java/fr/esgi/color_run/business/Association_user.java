package fr.esgi.color_run.business;

import lombok.Data;

@Data
public class Association_user {
    private Long id;
    private Long userId;
    private Long associationId;

    private static Long compteur = 0L;

    public Association_user() {
        this.id = compteur++;
    }
}
