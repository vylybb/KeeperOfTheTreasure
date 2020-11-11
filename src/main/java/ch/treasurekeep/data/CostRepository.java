package ch.treasurekeep.data;

import ch.treasurekeep.model.Cost;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CostRepository extends MongoRepository<Cost, String> {
}
