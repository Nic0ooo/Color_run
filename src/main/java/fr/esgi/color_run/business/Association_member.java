package fr.esgi.color_run.business;

import lombok.Data;

@Data
public class Association_member{
    private Long id;
    private Long memberId;
    private Long associationId;

    private static Long compteur = 0L;

    public Association_member() {
        this.id = compteur++;
    }
}
