package org.certis.siem.service;


import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.repository.EventRepository;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple4;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final String access_logs = "aws-access-logs-groups";
    private final String waf_logs = "aws-waf-logs-groups";
    private final String cloudtrail_logs = "cloudtrail-logs-groups";
    private final String http_logs = "/aws/lambda/http-gateway";
    private final String[] eventTypes = {"Cloud","Web", "WAF"};

    private final LocalDateTime defaultDate = LocalDateTime.of(2000, 11, 16, 0, 0);
    private final MetadataService metadataService;

    private final AccessLogsService accessLogsService;
    private final CloudTrailService cloudTrailService;
    private final HttpLogsService httpLogsService;
    private final WAFService wafService;
    private final EventRepository eventRepository;

    private int count=0;


    @Scheduled(fixedRate = 5 * 1000)  // 5s TEST
    public void checkForNewLogs() {
        Mono.zip(getAccessLogTimestamp(), getHttpLogsTimestamp(), getWAFLogTimestamp(), getCloudTrailLogsTimestamp())
                .flatMap(this::processTimestamp)
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
    }

    private Mono<Void> countAndLogEventCounts() {
        for (String eventType : eventTypes) {
            eventRepository.countByEventType(eventType)
                    .subscribe();
        }
        return Mono.empty();
    }


    private Mono<LocalDateTime> getAccessLogTimestamp(){
        return getLastProcessedTimestamp(access_logs)
                .flatMap(accessLogsService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);
    }

    private Mono<LocalDateTime> getHttpLogsTimestamp(){
        return getLastProcessedTimestamp(http_logs)
                .flatMap(httpLogsService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);
    }

    private Mono<LocalDateTime> getWAFLogTimestamp(){
        return getLastProcessedTimestamp(waf_logs)
                .flatMap(wafService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);
    }

    private Mono<LocalDateTime> getCloudTrailLogsTimestamp(){
        return getLastProcessedTimestamp(cloudtrail_logs)
                .flatMap(cloudTrailService::process)
                .filter(latestTimestamp -> latestTimestamp != null)
                .defaultIfEmpty(defaultDate);
    }

    private Mono<Void> processTimestamp(Tuple4<LocalDateTime, LocalDateTime, LocalDateTime, LocalDateTime> tuple){
        LocalDateTime accessLogsTimestamp = tuple.getT1();
        LocalDateTime httpLogsTimestamp = tuple.getT2();
        LocalDateTime wafLogsTimestamp = tuple.getT3();
        LocalDateTime cloudtrailLogsTimestamp = tuple.getT4();

        // TEST Log
        log.info("배치 작업 - (" + (++count) + ") Access Logs: " + accessLogsTimestamp+ ", WAF: " + wafLogsTimestamp+ ", Http: " + httpLogsTimestamp+ ", CloudTrail: " + cloudtrailLogsTimestamp);

        return updateMetadata(accessLogsTimestamp, httpLogsTimestamp, wafLogsTimestamp, cloudtrailLogsTimestamp);
    }

    private Mono<Void> updateMetadata(LocalDateTime accessLogsTimestamp, LocalDateTime httpLogsTimestamp,
                                      LocalDateTime wafLogsTimestamp, LocalDateTime cloudtrailLogsTimestamp){
        return countAndLogEventCounts()
                .then(Mono.zip(metadataService.updateTimestamp(access_logs, accessLogsTimestamp),
                        metadataService.updateTimestamp(http_logs, httpLogsTimestamp),
                        metadataService.updateTimestamp(waf_logs, wafLogsTimestamp),
                        metadataService.updateTimestamp(cloudtrail_logs, cloudtrailLogsTimestamp)
                ).then());
    }
}
