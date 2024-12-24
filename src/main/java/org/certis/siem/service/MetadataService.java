package org.certis.siem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.entity.Metadata;
import org.certis.siem.repository.MetaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataService {
    private final String NOT_FOUND_METADATA_ERROR_MESSAGE = "Not found metadata for logGroup: ";
    private final String FOUND_METADATA_MESSAGE = "Found metadata: ";
    private final String BLANK_MESSAGE = " ";

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
                        log.error(NOT_FOUND_METADATA_ERROR_MESSAGE + logGroup);
                    } else {
                        log.info(FOUND_METADATA_MESSAGE + metadata.getLogGroup() + BLANK_MESSAGE + metadata.getTimestamp());
                    }
                });
    }

}

