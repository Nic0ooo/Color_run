package fr.esgi.color_run.business;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Paiement {
    private Long id;
    private Long couseMemberId;
    private LocalDateTime date;
    private Double amount;

    private static Long compteur = 0L;

    public Paiement() {
        this.id = compteur++;
    }
}
