package ch.treasurekeep.data;

import ch.treasurekeep.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessagesRepository extends MongoRepository<Message, String> {
}
