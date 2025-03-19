package fr.esgi.color_run.business;

import lombok.Data;

@Data
public class Discussion {
    private Long id;
    private Long courseId;
    private boolean isActive;

    private static Long compteur = 0L;

    public Discussion() {
        this.id = compteur++;
    }

}
