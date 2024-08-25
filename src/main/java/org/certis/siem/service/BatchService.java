package org.certis.siem.service;


import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.Metadata;
import org.certis.siem.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final MetadataService metadataService;
    private final EventRepository eventRepository;

    private final AccessLogsService accessLogsService;
    private final OpenSearchService openSearchService;

    @Scheduled(fixedRate = 60 * 1000)  // 1min
    public void checkForNewLogs() {
        getLastProcessedTimestamp()
                .flatMapMany(lastProcessedTimestamp ->
                        Flux.concat(
                                accessLogsService.processAccessLogs(lastProcessedTimestamp),
                                openSearchService.processWAFLogs()
                        )
                )
                .flatMap(event -> eventRepository.save(event))
                .then(metadataService.updateTimestamp("aws-access-logs-groups", LocalDateTime.now()))
                .then(metadataService.updateTimestamp("aws-waf-logs-groups", LocalDateTime.now()))

                .subscribe();
    }

    private Mono<LocalDateTime> getLastProcessedTimestamp() {
        return metadataService.getLastTimestampForLogGroup("aws-access-logs-groups")
                .map(Metadata::getTimestamp)
                .defaultIfEmpty(LocalDateTime.now().minusDays(1));
    }


}
