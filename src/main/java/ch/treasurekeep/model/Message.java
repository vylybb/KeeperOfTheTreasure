package ch.treasurekeep.model;

import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

/**
 * A Message triggered by a event
 * One example might be a soft-threshold being reached
 */
public class Message {

    @Id
    private String id;
    private String subject;
    private String content;
    private LocalDateTime localDateTime;

    public Message(String subject, String content) {
        this.subject = subject;
        this.content = content;
        this.localDateTime = LocalDateTime.now();
    }
    public String getSubject() { return subject; }

    public String getContent() { return content; }

    public LocalDateTime getLocalDateTime() { return localDateTime; }
}
