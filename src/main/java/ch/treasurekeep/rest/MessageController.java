package ch.treasurekeep.rest;

import ch.treasurekeep.data.MessagesRepository;
import ch.treasurekeep.model.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The message controller is responsible for all REST-calls.
 * that are concerning the Messages
 * These are all Messages sent via mail
 */
@RestController
@RequestMapping("/")
public class MessageController {

    private MessagesRepository messagesRepository;
    public MessageController(MessagesRepository messagesRepository) {
        this.messagesRepository = messagesRepository;
    }
    @GetMapping("messages")
    public List<Message> getMessages() {
        return this.messagesRepository.findAll();
    }
}
