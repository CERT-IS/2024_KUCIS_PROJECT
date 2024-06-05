package org.certis.siem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.certis.siem.repository.CloudTrailRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudTrailService {
    private final CloudTrailRepository cloudTrailRepository;

    public Flux<CloudTrailEvent> findAll() {
        return cloudTrailRepository.findAll();
    }
    public Mono<CloudTrailEvent> getEventById(String id) {
        return cloudTrailRepository.findById(id);
    }

    public Flux<CloudTrailEvent> getEventsByRegion(String region) {
        return cloudTrailRepository.findByAwsRegion(region);
    }
}


