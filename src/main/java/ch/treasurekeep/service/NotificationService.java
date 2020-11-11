package ch.treasurekeep.service;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.data.MessagesRepository;
import ch.treasurekeep.model.Log;
import ch.treasurekeep.model.Message;
import org.springframework.stereotype.Service;

/**
 * Will Forward a notifications to other services
 */
@Service
public class NotificationService {
    private MessagesRepository messageRepository;
    private EmailService emailService;
    private LogRepository logRepository;
    public NotificationService(LogRepository logRepository, EmailService emailService, MessagesRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.emailService = emailService;
        this.logRepository = logRepository;
    }

    public void sendNotification(String subject, String content) {
        try{
            this.messageRepository.insert(new Message(subject, content));
            this.emailService.sendEmail("Treasurekeep" + subject , content);
        }
        catch (Exception e) {
            this.logRepository.insert(new Log(NotificationService.class.getName(), e.getMessage()));
        }
    }
}
