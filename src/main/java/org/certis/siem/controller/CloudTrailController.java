package org.certis.siem.controller;

import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.certis.siem.entity.EventLog;
import org.certis.siem.service.CloudTrailService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/cloudTrail")
@RequiredArgsConstructor
public class CloudTrailController {

    private final CloudTrailService cloudTrailService;


    @GetMapping("")
    public Flux<CloudTrailEvent> getAllEvents() {
        return cloudTrailService.findAll();
    }

    @GetMapping("/events/region/{region}")
    public Flux<CloudTrailEvent> getEventsByRegion(@PathVariable String region) {
        return cloudTrailService.getEventsByRegion(region);
    }

    @GetMapping("/event/{id}")
    public Mono<CloudTrailEvent> getEventById(@PathVariable String id) {
        return cloudTrailService.getEventById(id);
    }


    @GetMapping("/events/region/detect")
    public Flux<EventLog> getEventsByRegionNotIn(@RequestParam List<String> regions) {
        return cloudTrailService.getEventsByRegionNotIn(regions);
    }

    @GetMapping("/events/logs/s3")
    public Mono<?> saveEventsS3toElasticSearch(){
        return cloudTrailService.saveLogsS3toElasticSearch();
    }
}
