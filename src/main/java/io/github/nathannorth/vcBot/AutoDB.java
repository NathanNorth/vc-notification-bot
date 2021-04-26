package io.github.nathannorth.vcBot;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public class AutoDB {
    public interface DBRepository extends ReactiveCrudRepository<Long, Long> {

        @Query("SELECT userID FROM chans WHERE channelID = :name")
        Flux<Integer> findByChan(Long name);

    }
}
