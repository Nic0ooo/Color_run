package fr.esgi.color_run.business;

import lombok.Data;

@Data
public class Course_member {
    private Long id;
    private Long courseId;
    private Long memberId;
    private String registrationDate;
    private Status registrationStatus;
    private String stripeSessionId;

    private static Long compteur = 0L;

    public Course_member() {
        this.id = compteur++;
        this.registrationStatus = Status.PENDING;
    }
}
