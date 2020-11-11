package ch.treasurekeep.model;

import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

/**
 * Represents a Log entry
 * (This project does not use java-loggers, instead Errors are directly written into the mongo-database)
 */
public class Log {

    @Id
    private String identifier;
    private String source;
    private String message;
    private String level;
    private LocalDateTime localDateTime;

    public Log() {
    }

    public Log(String source, String message) {
        this.localDateTime = LocalDateTime.now();
        this.source = source;
        this.level = "standard"; //this is the only used so far
        this.message = message;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getLocalDateTime() { return localDateTime; }
    public void setLocalDateTime(LocalDateTime localDateTime) { this.localDateTime = localDateTime; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
