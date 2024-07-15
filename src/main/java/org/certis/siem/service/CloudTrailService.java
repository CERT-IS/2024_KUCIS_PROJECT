package org.certis.siem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.certis.siem.entity.CloudTrail.CloudTrailEventList;
import org.certis.siem.entity.EventLog;
import org.certis.siem.repository.CloudTrailRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudTrailService {
    private final CloudTrailRepository cloudTrailRepository;
    private final AwsS3Service awsS3Service;
    private final EventDetectService eventDetectService;

    private final List<String> awsRegions = Arrays.asList(
            "us-east-1", "us-east-2", "us-west-1", "us-west-2",
            "ap-east-1", "ap-south-1", "ap-northeast-1", "ap-northeast-2",
            "ap-northeast-3", "ap-southeast-1", "ap-southeast-2",
            "ca-central-1", "eu-central-1", "eu-west-1", "eu-west-2",
            "eu-west-3", "eu-north-1", "sa-east-1", "me-south-1",
            "af-south-1", "eu-south-1", "eu-south-2"
    );

    private final ObjectMapper mapper = new ObjectMapper();


    public Flux<CloudTrailEvent> findAll() {
        return cloudTrailRepository.findAll();
    }
    public Mono<CloudTrailEvent> getEventById(String id) {
        return cloudTrailRepository.findById(id);
    }

    public Flux<CloudTrailEvent> getEventsByRegion(String region) {
        return cloudTrailRepository.findByAwsRegion(region);
    }

    public Flux<EventLog> getEventsByRegionNotIn(List<String> excludeds) {
        List<String> includeds = awsRegions.stream()
                .filter(region -> !excludeds.contains(region))
                .collect(Collectors.toList());

        return cloudTrailRepository.findByAwsRegionIn(includeds)
                .flatMap(event -> {
                    String type = "cloudtail";
                    EventLog eventLog = EventLog.builder()
                            .type(type)
                            .cloudTrailEvent(event).build();

                    log.info("Log register : " + eventLog.toString());

                    return eventDetectService.register(eventLog);});
    }

    public Mono<Void> saveLogsS3toElasticSearch() {
        return awsS3Service.downloadAllFiles("AWSLogs/058264524253/CloudTrail/ap-northeast-2/2024/06/02/")
                .concatMap(content -> {
                        List<CloudTrailEvent> events = parseToCloudTrailEvents(content);
                        return cloudTrailRepository.saveAll(events);

                })
                .doOnError(e -> log.error("error : cant saving logs (S3 to Elasticsearch)", e))
                .then();
    }


    private List<CloudTrailEvent> parseToCloudTrailEvents(String jsonString) {
        try {
            return mapper.readValue(jsonString, CloudTrailEventList.class).getRecords();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON to CloudTrailEvent :"+ jsonString, e);
        }
    }
}


