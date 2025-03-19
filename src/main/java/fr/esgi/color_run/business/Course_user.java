package fr.esgi.color_run.business;

import lombok.Data;

@Data
public class Course_user {
    private Long id;
    private Long courseId;
    private Long userId;

    private static Long compteur = 0L;

    public Course_user() {
        this.id = compteur++;
    }
}
