package org.certis.siem.controller;

import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.EventStream;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.service.EventDetectService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;


@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventDetectService eventDetectService;

    @GetMapping("/detect")
    public Flux<EventStream> getEvents(@RequestParam("lastTimestamp") Instant lastTimestamp, @RequestParam("size") int size,@RequestParam("offset") int offset) {
        String eventType = "WAF";
        return eventDetectService.findByEventTypeNot(lastTimestamp, size,offset, eventType);
    }

    @GetMapping("/detect/waf")
    public Flux<EventStream> getFirewallEvents(@RequestParam("lastTimestamp") Instant lastTimestamp, @RequestParam("size") int size, @RequestParam("offset") int offset) {
        String eventType = "WAF";
        return eventDetectService.findByEventType(lastTimestamp, size, offset,eventType);
    }

    @GetMapping("/all")
    public Flux<EventStream> getAllEvents(){
        return eventDetectService.findAll();
    }


}
