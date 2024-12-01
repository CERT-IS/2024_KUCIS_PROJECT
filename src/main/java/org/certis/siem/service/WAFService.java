package org.certis.siem.service;

import static org.certis.siem.mapper.EventMapper.mapLogsToEvent;
import static org.certis.siem.service.OpenSearchService.executeSearch;

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
    private final EventRepository eventRepository;
    private final String indexName = "cwl-*";
    private final String logGroup = "aws-waf-logs-groups";

    private final String[] XSS_PATTERNS = {
            "<script.*?>.*?</script>",
            "javascript:",
            "on\\w+=['\"].*?['\"]",
            "<.*?javascript:.*?>",
            "<.*?on\\w+=.*?>"
    };

    private static final Pattern URL_PATTERN = Pattern.compile("\"(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH)\\s+(http[^\"]+)\\s+HTTP/1\\.1\"");


    public Flux<EventStream> checkXSS(Flux<EventStream> logs) {
        return logs.filter(accessLog -> {
            AccessLog log = (AccessLog) accessLog.getLogs();
            String message= log.getMessage();
            String url = extractUrl(message);
            if (url != null) {
                String decodedUrl = decodeUrl(url);
                for (String pattern : XSS_PATTERNS) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(decodedUrl).find()) {
                        System.out.println("XSS Injection Detected: " + message);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private String extractUrl(String logEntry) {
        Matcher matcher = URL_PATTERN.matcher(logEntry);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }
    private String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return url;
        }
    }


    public Mono<LocalDateTime> process(LocalDateTime lastProcessedTimestamp) {
        SearchRequest searchRequest = searchRequest(lastProcessedTimestamp);
        return executeSearch(searchRequest)
                .collectList()
                .flatMap(jsonNodes -> {
                    Flux<EventStream> eventStreamFlux = mapJsonToEventStream(jsonNodes);

                    List<WAFLog> wafList = jsonNodes.stream()
                            .map(WAFMapper::mapJsonNodeToWAFEvent)
                            .collect(Collectors.toList());

                    LocalDateTime latestTimestamp = getLatestTimestamp(wafList, lastProcessedTimestamp);


                    return eventStreamFlux
                            .collectList()
                            .flatMap(eventStreams -> eventRepository.saveAll(eventStreams)
                                    .then(Mono.just(latestTimestamp))
                                    .onErrorMap(e -> new RuntimeException("Error processing Accesslogs: " + e.getMessage(), e))
                            );
                });
    }

    public LocalDateTime getLatestTimestamp(List<WAFLog> wafLogs, LocalDateTime timestamp) {
        return wafLogs.stream()
                .map(WAFLog::getTimestamp)
                .max(Comparator.naturalOrder())
                .orElse(timestamp);
    }


    private SearchRequest searchRequest(LocalDateTime timestamp) {

        String timestampString = timestamp.format(DateTimeFormatter.ISO_DATE_TIME);

        Query query = Query.of(q -> q.bool(b -> b
                .must(m -> m.match(t -> t.field("@log_group.keyword").query(FieldValue.of(logGroup))))
                .filter(f -> f.range(r -> r.field("@timestamp").gt(JsonData.of(timestampString))))
        ));

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id", "@log_group", "@timestamp", "terminatingRuleType", "terminatingRuleId", "action", "httpRequest.country", "httpRequest.clientIp"));
        SourceConfig sourceConfig = SourceConfig.of(src -> src.filter(sourceFilter));

        return SearchRequest.of(s -> s.index(indexName)
                .source(sourceConfig)
                .query(query)
                .sort(sort -> sort.field(f -> f.field("@timestamp").order(SortOrder.Asc)))
                .size(30));
    }

    public Flux<EventStream> mapJsonToEventStream(List<JsonNode> jsonNodes) {
        List<WAFLog> wafLogs = jsonNodes.stream()
                .map(WAFMapper::mapJsonNodeToWAFEvent)
                .collect(Collectors.toList());

        return Flux.fromIterable(wafLogs)
                .map(wafLog -> mapLogsToEvent("WAF EventStream", "WAF", wafLog));
    }
}
