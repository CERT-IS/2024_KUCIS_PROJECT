package org.certis.siem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jdk.jfr.Event;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.dto.SearchRequest;
import org.certis.siem.service.AccessLogsService;
import org.certis.siem.service.CloudTrailService;
import org.certis.siem.service.OpenSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/opensearch")
@RequiredArgsConstructor
public class OpenSearchController {

    private final OpenSearchService openSearchService;
    private final AccessLogsService accessLogsService;
    private final CloudTrailService cloudTrailService;

    @GetMapping("/documents/waf-logs")
    public Flux<JsonNode> getDocumentsByWAF(@RequestParam String timestamp) {
        LocalDateTime lastProcessedTimestamp;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            lastProcessedTimestamp = LocalDateTime.parse(timestamp, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid timestamp format: " + timestamp);
            return Flux.empty();
        }

        return openSearchService.executeSearch(lastProcessedTimestamp);

        // indexName = cwl-*
        // @log_group = 'aws-cloudtrail-logs-058264524253-eba56a76', 'aws-access-logs-groups', 'aws-waf-logs-groups'
    }

    @GetMapping("/check-field-existence/{fieldName}")
    public Mono<List<JsonNode>> checkFieldExistence(@PathVariable String fieldName) {
        return openSearchService.checkFieldExistence(fieldName);
    }

    @GetMapping("/cloudtrail/exclude-regions")
    public Mono<ResponseEntity<List<String>>> getExcludeRegions() {
        return cloudTrailService.getExcludeRegions()
                .map(ResponseEntity::ok);
    }

    @PostMapping("/cloudtrail/exclude-regions")
    public ResponseEntity<Void> addExcludeRegions(@RequestBody List<String> regions) {
        cloudTrailService.addExcludeRegions(regions);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cloudtrail/exclude-regions")
    public ResponseEntity<Void> deleteExcludeRegions(@RequestBody List<String> regions) {
        cloudTrailService.deleteExcludeRegions(regions);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/query")
    public ResponseEntity<Flux<JsonNode>> executeQueryStringSearch(
            @RequestBody String queryString) {

        Flux<JsonNode> results = openSearchService.executeQueryStringSearch(queryString);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/conditional")
    public ResponseEntity<Flux<JsonNode>> executeConditionalSearch(
            @RequestBody SearchRequest searchRequest,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("reqeust: "+ searchRequest.toString());

        Flux<JsonNode> results = openSearchService.executeConditionalSearch(searchRequest.getNewIndex(),
                searchRequest.getLogGroup(),
                searchRequest.getWhereClause(),
                searchRequest.getStartDate(),
                searchRequest.getEndDate(),
                searchRequest.getFields(),
                size);

        return ResponseEntity.ok(results);
    }
}
