package org.certis.siem.repository;


import org.certis.siem.entity.EventStream;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.Instant;

public interface EventRepository extends ReactiveCrudRepository<EventStream, Long> {

    @Query("SELECT * FROM eventStream WHERE event_type = :eventType AND timestamp > :lastTimestamp ORDER BY timestamp ASC, id ASC LIMIT :size OFFSET :offset")
    Flux<EventStream> findByEventTypeAfterTimestamp(@Param("eventType") String eventType,
                                                    @Param("lastTimestamp") Instant lastTimestamp,
                                                    @Param("size") int size,
                                                    @Param("offset") int offset);

    @Query("SELECT * FROM eventStream WHERE event_type != :eventType AND timestamp > :lastTimestamp ORDER BY timestamp ASC, id ASC LIMIT :size OFFSET :offset")
    Flux<EventStream> findByEventTypeNotAfterTimestamp(@Param("eventType") String eventType,
                                                       @Param("lastTimestamp") Instant lastTimestamp,
                                                       @Param("size") int size,
                                                       @Param("offset") int offset);
}
