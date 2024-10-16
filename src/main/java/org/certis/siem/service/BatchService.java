package org.certis.siem.service;


import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.Metadata;
import org.certis.siem.repository.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BatchService {

    private int count=0;
    private final LocalDateTime defaultDate = LocalDateTime.of(2000, 11, 16, 0, 0);
    private final MetadataService metadataService;

    private final AccessLogsService accessLogsService;
    private final OpenSearchService openSearchService;
    private final EventRepository eventRepository;

    private final String access_logs = "aws-access-logs-groups";
    private final String waf_logs = "aws-waf-logs-groups";

    @Scheduled(fixedRate = 5 * 1000)  // 5s
    public void checkForNewLogs() {
        Mono<LocalDateTime> accessLogsTimestampMono = getLastProcessedTimestamp(access_logs)
                .flatMap(accessLogsService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);

        Mono<LocalDateTime> wafLogsTimestampMono = getLastProcessedTimestamp(waf_logs)
                .flatMap(openSearchService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);

        Mono.zip(accessLogsTimestampMono, wafLogsTimestampMono)
                .flatMap(tuple -> {
                    LocalDateTime accessLogsTimestamp = tuple.getT1();
                    LocalDateTime wafLogsTimestamp = tuple.getT2();

                    System.out.println("배치 작업 - (" + (++count) + ") Access Logs: " + accessLogsTimestamp+ ", WAF: " + wafLogsTimestamp);

                    return countAndLogEventCounts()
                            .then(Mono.zip(
                                    metadataService.updateTimestamp(access_logs, accessLogsTimestamp),
                                    metadataService.updateTimestamp(waf_logs, wafLogsTimestamp)
                            ));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }


    private Mono<LocalDateTime> getLastProcessedTimestamp(String logGroup) {
        return metadataService.getLastTimestampForLogGroup(logGroup)
                .map(metadata -> {
                    LocalDateTime timestamp = metadata.getTimestamp();
                    return timestamp;
                })
                .defaultIfEmpty(defaultDate);
                //.doOnSuccess(timestamp -> System.out.println("Returned timestamp : "+timestamp+" - loggroup:"+logGroup));
    }

    private Mono<Void> countAndLogEventCounts() {
        String[] eventTypes = {"Cloud","Web", "WAF"};

        for (String eventType : eventTypes) {
            eventRepository.countByEventType(eventType)
                    .doOnNext(count -> System.out.println("Total count for eventType " + eventType + ": " + count))
                    .subscribe();
        }

        return Mono.empty();
    }

}
