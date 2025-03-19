package fr.esgi.color_run.business;

import lombok.Data;

@Data
public class Message {
    private Long id;
    private Long discussionId;
    private Long memberId;
    private String content;
    private String date;
    private boolean isPin;
    private boolean isHidden;

    private static Long compteur = 0L;

    public Message() {
        this.id = compteur++;
    }
}
