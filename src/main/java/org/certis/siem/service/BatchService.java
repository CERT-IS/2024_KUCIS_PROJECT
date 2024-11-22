package org.certis.siem.service;


import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.Metadata;
import org.certis.siem.repository.EventRepository;
import org.opensearch.client.opensearch.nodes.Http;
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
    private final CloudTrailService cloudTrailService;
    private final HttpLogsService httpLogsService;
    private final EventRepository eventRepository;

    private final String access_logs = "aws-access-logs-groups";
    private final String waf_logs = "aws-waf-logs-groups";
    private final String cloudtrail_logs = "cloudtrail-logs-groups";
    private final String http_logs = "/aws/lambda/http-gateway";

    @Scheduled(fixedRate = 5 * 1000)  // 5s
    public void checkForNewLogs() {
        Mono<LocalDateTime> accessLogsTimestampMono = getLastProcessedTimestamp(access_logs)
                .flatMap(accessLogsService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);

        Mono<LocalDateTime> httpLogsTimestampMono = getLastProcessedTimestamp(http_logs)
                .flatMap(httpLogsService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);

        Mono<LocalDateTime> wafLogsTimestampMono = getLastProcessedTimestamp(waf_logs)
                .flatMap(openSearchService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);

        Mono<LocalDateTime> cloudtrailTimestampMono = getLastProcessedTimestamp(cloudtrail_logs)
                .flatMap(cloudTrailService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);


        Mono.zip(accessLogsTimestampMono, httpLogsTimestampMono, wafLogsTimestampMono, cloudtrailTimestampMono)
                .flatMap(tuple -> {
                    LocalDateTime accessLogsTimestamp = tuple.getT1();
                    LocalDateTime httpLogsTimestamp = tuple.getT2();
                    LocalDateTime wafLogsTimestamp = tuple.getT3();
                    LocalDateTime cloudtrailLogsTimestamp = tuple.getT4();

                    System.out.println("배치 작업 - (" + (++count) + ") Access Logs: " + accessLogsTimestamp+ ", WAF: " + wafLogsTimestamp+ ", Http: " + httpLogsTimestamp+ ", CloudTrail: " + cloudtrailLogsTimestamp);

                    return countAndLogEventCounts()
                            .then(Mono.zip(
                                    metadataService.updateTimestamp(access_logs, accessLogsTimestamp),
                                    metadataService.updateTimestamp(http_logs, httpLogsTimestamp),
                                    metadataService.updateTimestamp(waf_logs, wafLogsTimestamp),
                                    metadataService.updateTimestamp(cloudtrail_logs, cloudtrailLogsTimestamp)
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
