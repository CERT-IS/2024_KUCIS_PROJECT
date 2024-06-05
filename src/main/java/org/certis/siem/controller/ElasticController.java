package org.certis.siem.controller;


import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexInformation;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/elastic-cluster")
public class ElasticController {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;
    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @GetMapping("/info")
    public Flux<IndexInformation> getClusterInfo() { // CloudTrailEvent 인덱스를 반환
        return reactiveElasticsearchTemplate.indexOps(CloudTrailEvent.class).getInformation();
    }

    @DeleteMapping("/clear-indices")
    public Mono<String> clearIndices() { // CloudTrailEvent 인덱스를 삭제
        return reactiveElasticsearchOperations.indexOps(CloudTrailEvent.class).delete().flatMap(this::getDeleted);
    }

    private Mono<String> getDeleted(Boolean isDeleted) {
        if (isDeleted) {
            return Mono.just("Deleted");
        } else {
            return Mono.just("Unable to delete");
        }
    }
}
