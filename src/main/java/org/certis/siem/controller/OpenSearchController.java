package org.certis.siem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.dto.SearchRequest;
import org.certis.siem.service.AccessLogsService;
import org.certis.siem.service.CloudTrailService;
import org.certis.siem.service.OpenSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/opensearch")
@RequiredArgsConstructor
public class OpenSearchController {

    private final OpenSearchService openSearchService;
    private final AccessLogsService accessLogsService;
    private final CloudTrailService cloudTrailService;

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
    public Mono<ResponseEntity<Void>> addExcludeRegions(@RequestBody List<String> regions) {
        return cloudTrailService.addExcludeRegions(regions)
                .thenReturn(ResponseEntity.ok().build());
    }

    @DeleteMapping("/cloudtrail/exclude-regions")
    public Mono<ResponseEntity<Void>> deleteExcludeRegions(@RequestBody List<String> regions) {
        return cloudTrailService.deleteExcludeRegions(regions)
                .thenReturn(ResponseEntity.ok().build());
    }


    @PostMapping("/query")
    public ResponseEntity<Flux<JsonNode>> executeQueryStringSearch(@RequestBody String queryString) {
        Flux<JsonNode> results = openSearchService.executeQueryStringSearch(queryString);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/conditional")
    public ResponseEntity<Flux<JsonNode>> executeConditionalSearch(@RequestBody SearchRequest searchRequest, @RequestParam(defaultValue = "10") int size) {
        Flux<JsonNode> results = openSearchService.executeConditionalSearch(searchRequest, size);
        return ResponseEntity.ok(results);
    }
}