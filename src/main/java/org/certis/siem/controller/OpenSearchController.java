package org.certis.siem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.certis.siem.service.OpenSearchService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/opensearch")
@RequiredArgsConstructor
public class OpenSearchController {

    private final OpenSearchService openSearchService;


    @GetMapping("/documents/{logGroup}")
    public Flux<JsonNode> getDocumentsByLogGroup(@PathVariable String logGroup) {
        return openSearchService.getDocumentsByLogGroup(logGroup);

        // indexName = cwl-*
        // @log_group = 'aws-cloudtrail-logs-058264524253-eba56a76', '/aws/alb/access-logs', 'aws-waf-logs-groups'

    }

    @GetMapping("/documents/access-logs")
    public Flux<JsonNode> getDocumentsByAccessLogs() {
        return openSearchService.processAccessLogs();
    }



    @GetMapping("/check-field/{fieldName}")
    public Mono<List<JsonNode>> checkFieldExistence(@PathVariable String fieldName) {
        return openSearchService.checkFieldExistence(fieldName);
    }
}
