package org.certis.siem.repository;

import org.certis.siem.entity.Metadata;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MetaRepository extends ReactiveCrudRepository<Metadata, String> {

    @Query("SELECT * FROM metadata WHERE log_group = :logGroup")
    Mono<Metadata> findByLogGroup(String logGroup);
}
