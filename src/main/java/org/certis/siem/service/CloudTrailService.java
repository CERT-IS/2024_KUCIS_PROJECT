package org.certis.siem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.dto.RegionRequest;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.certis.siem.repository.CloudTrailRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudTrailService {
    private final CloudTrailRepository cloudTrailRepository;
    private final AwsS3Service awsS3Service;
    private final List<String> awsRegions = Arrays.asList(
            "us-east-1", "us-east-2", "us-west-1", "us-west-2",
            "ap-east-1", "ap-south-1", "ap-northeast-1", "ap-northeast-2",
            "ap-northeast-3", "ap-southeast-1", "ap-southeast-2",
            "ca-central-1", "eu-central-1", "eu-west-1", "eu-west-2",
            "eu-west-3", "eu-north-1", "sa-east-1", "me-south-1",
            "af-south-1", "eu-south-1", "eu-south-2"
    );


    public Flux<CloudTrailEvent> findAll() {
        return cloudTrailRepository.findAll();
    }
    public Mono<CloudTrailEvent> getEventById(String id) {
        return cloudTrailRepository.findById(id);
    }

    public Flux<CloudTrailEvent> getEventsByRegion(String region) {
        return cloudTrailRepository.findByAwsRegion(region);
    }

    public Flux<CloudTrailEvent> getEventsByRegionNotIn(RegionRequest regionRequest) {
        List<String> excludeds = regionRequest.getRegions();
        List<String> includeds = awsRegions.stream()
                .filter(region -> !excludeds.contains(region))
                .collect(Collectors.toList());

        return cloudTrailRepository.findByAwsRegionIn(includeds);
        /*     .flatMap(event -> {
                    String eventLog = objectMapper.writeValueAsString(event);
                    String eventName = event.getEventName();
                    return awsS3Service.upload(eventLog, eventName).thenReturn(event);
                })
        */
    }

    public Mono<Void> saveLogsS3toElasticSearch() {
        return awsS3Service.downloadAllFiles()
                .concatMap(filePath -> {
                    try {
                        List<String> lines = Files.readAllLines(Paths.get(filePath));
                        List<CloudTrailEvent> events = lines.stream()
                                .map(this::parseToCloudTrailEvent)
                                .collect(Collectors.toList());
                        return cloudTrailRepository.saveAll(events);
                    } catch (IOException e) {
                        return Flux.error(new RuntimeException("Failed to read file", e));
                    }
                })
                .then();
    }

    private CloudTrailEvent parseToCloudTrailEvent(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, CloudTrailEvent.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON to CloudTrailEvent", e);
        }
    }
}


