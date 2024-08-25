package org.certis.siem.service;

import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.Metadata;
import org.certis.siem.repository.MetaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final MetaRepository metaRepository;

    public Mono<Metadata> updateTimestamp(String logGroup, LocalDateTime timestamp) {
        return metaRepository.findByLogGroup(logGroup)
                .flatMap(metadata -> {
                    metadata.setTimestamp(timestamp);
                    return metaRepository.save(metadata);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setLogGroup(logGroup);
                    metadata.setTimestamp(timestamp);
                    return metaRepository.save(metadata);
                }));
    }

    public Mono<Metadata> getLastTimestampForLogGroup(String logGroup) {
        return metaRepository.findByLogGroup(logGroup)
                .doOnNext(metadata -> {
                    if (metadata == null) {
                        System.out.println("No metadata found for logGroup: " + logGroup);
                    } else {
                        System.out.println("Found metadata: " + metadata);
                    }
                });
    }

}

