package org.certis.siem.repository;

import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface CloudTrailRepository extends ReactiveElasticsearchRepository<CloudTrailEvent, String> {

    Flux<CloudTrailEvent> findByAwsRegion(String awsRegion);
    Flux<CloudTrailEvent> findByAwsRegionIn(List<String> awsRegion);
}
