package org.certis.siem.service;

import static org.certis.siem.mapper.EventMapper.mapLogsToEvent;
import static org.certis.siem.service.OpenSearchService.getSearchQuery;
import static org.certis.siem.service.OpenSearchService.getSearchSort;
import static org.certis.siem.service.OpenSearchService.searchRequestSize;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.log.AccessLog;
import org.certis.siem.entity.log.WAFLog;
import org.certis.siem.mapper.WAFMapper;
import org.certis.siem.repository.EventRepository;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WAFService {

    private final String WAF_PROCESS_ERROR_MESSAGE = "Error processing WAF Logs: ";

    private final String indexName = "cwl-*";
    private final String logGroup = "aws-waf-logs-groups";

    private final OpenSearchService openSearchService;
    private final EventRepository eventRepository;



    public Mono<LocalDateTime> process(LocalDateTime lastProcessedTimestamp) {
        return openSearchService.executeSearch(searchRequest(lastProcessedTimestamp))
                .collectList()
                .flatMap(jsonNodes -> mapJsonToEventStream(jsonNodes).collectList()
                        .flatMap(eventStreams -> updateEvent(eventStreams, jsonNodes, lastProcessedTimestamp)));
    }

    private Mono<LocalDateTime> updateEvent(List<EventStream> eventStreams, List<JsonNode> jsonNodes, LocalDateTime lastProcessedTimestamp){
        return eventRepository.saveAll(eventStreams)
                .then(Mono.just(getLatestTimestamp(getWAFList(jsonNodes), lastProcessedTimestamp)))
                .onErrorMap(e -> new RuntimeException(WAF_PROCESS_ERROR_MESSAGE + e.getMessage(), e));
    }

    private List<WAFLog> getWAFList(List<JsonNode> jsonNodes){
        return jsonNodes.stream()
                .map(WAFMapper::mapJsonNodeToWAFEvent)
                .collect(Collectors.toList());
    }

    public LocalDateTime getLatestTimestamp(List<WAFLog> wafLogs, LocalDateTime timestamp) {
        return wafLogs.stream()
                .map(WAFLog::getTimestamp)
                .max(Comparator.naturalOrder())
                .orElse(timestamp);
    }

    public Flux<EventStream> mapJsonToEventStream(List<JsonNode> jsonNodes) {
        List<WAFLog> wafLogs = jsonNodes.stream()
                .map(WAFMapper::mapJsonNodeToWAFEvent)
                .collect(Collectors.toList());

        return Flux.fromIterable(wafLogs)
                .map(wafLog -> mapLogsToEvent("WAF EventStream", "WAF", wafLog));
    }

    private SearchRequest searchRequest(LocalDateTime timestamp) {
        return SearchRequest.of(s -> s.index(indexName)
                .source(getSearchFilterConfig())
                .query(getSearchQuery(logGroup, timestamp))
                .sort(sort -> getSearchSort(sort))
                .size(searchRequestSize));
    }


    private SourceConfig getSearchFilterConfig(){
        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id",
                        "@log_group",
                        "@timestamp",
                        "terminatingRuleType",
                        "terminatingRuleId",
                        "action",
                        "httpRequest.country",
                        "httpRequest.clientIp"));

        return SourceConfig.of(src -> src.filter(sourceFilter));
    }
}
