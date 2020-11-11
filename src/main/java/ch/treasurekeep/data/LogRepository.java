package ch.treasurekeep.data;

import ch.treasurekeep.model.Log;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<Log, String> {
}
