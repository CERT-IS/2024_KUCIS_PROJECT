package org.certis.siem.controller;

import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.WAF.WAFEvent;
import org.certis.siem.service.WAFService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/waf")
@RequiredArgsConstructor
public class WAFController {
    private final WAFService wafService;

    @GetMapping("/events/detect")
    public Flux<WAFEvent> getWAFEventsAll() {
        return wafService.findAll();
    }
}
