package org.certis.siem.controller;

import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.EventLog;
import org.certis.siem.entity.EventStream;
import org.certis.siem.service.EventDetectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventDetectService eventDetectService;

    @GetMapping("/detect/all/{id}")
    public Mono<EventStream> getEventStreamByType(@PathVariable Long id){
        return eventDetectService.getEventStreamByType(id);
    }

    @GetMapping("/detect")
    public Mono<List<EventStream>> getEvents() {
        System.out.println("getEvents");
        return eventDetectService.getEventStreams().collectList();
    }

    @GetMapping("/detect/dangrous")
    public Mono<List<EventStream>> getDangerousEvents(){
        return eventDetectService.getDangerousEvents().collectList();
    }


    @PostMapping("/dummy")
    public Mono<String> registerDummy() {
        EventLog eventLog1 = EventLog.builder()
                .type("Type1:Event")
                .build();

        EventLog eventLog2 = EventLog.builder()
                .type("Type2:Event")
                .build();

        EventLog eventLog3 = EventLog.builder()
                .type("Type3:Event")
                .build();

        eventDetectService.register(eventLog1).subscribe();
        eventDetectService.register(eventLog2).subscribe();
        eventDetectService.register(eventLog3).subscribe();

        return Mono.just("Dummy events registered");
    }

}
