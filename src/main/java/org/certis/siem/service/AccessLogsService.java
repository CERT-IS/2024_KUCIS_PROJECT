package org.certis.siem.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.log.AccessLog;
import org.certis.siem.entity.EventStream;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.mapper.AccessLogsMapper;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.certis.siem.mapper.EventMapper.mapLogsToEvent;
import static org.certis.siem.service.OpenSearchService.executeSearch;

@Service
@RequiredArgsConstructor
public class AccessLogsService {

    private final OpenSearchClient openSearchClient;
    private final EventRepository eventRepository;

    private final String indexName = "cwl-*";
    private final String logGroup = "aws-access-logs-groups"; //"/aws/lambda/http-gateway";

    private static final Pattern URL_PATTERN = Pattern.compile("\"(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH)\\s+(http[^\"]+)\\s+HTTP/1\\.1\"");
    private final String[] SQL_INJECTION_PATTERNS = {
            "(?i)\\b(SELECT|UPDATE|DELETE|INSERT|UNION|DROP|ALTER)\\b",
            "(?i)\\bOR\\b\\s+\\d+=\\d+",
            "(?i)\\bOR\\b\\s+[\\w'\"=]+",
            "(--|#|;|/\\*|\\*/)",
            "(?i)\\bUNION\\b\\s*\\bSELECT\\b",
            "(?i)'\\s*--",
            "(?i)\"\\s*--",
            "(?i)\\bEXEC\\b\\s+\\bXP_",
    };
    private final String[] XSS_PATTERNS = {
            "<script.*?>.*?</script>",
            "javascript:",
            "on\\w+=['\"].*?['\"]",
            "<.*?javascript:.*?>",
            "<.*?on\\w+=.*?>"
    };
    private final String[] ADMIN_PAGE_PATTERNS = {
            "(?i)^/admin(?:[0-9]+|istrator)?/?$",
            "(?i)^/(admin(?:[0-9]+|istrator)?)/(.*\\.(html|asp))$",
            "(?i)^/(webadmin|manager|master|system)?/?$",
            "(?i)^/(webadmin|manager|master|system)/(.*\\.(html|asp))$"
    };

    private String extractUrl(String logEntry) {
        Matcher matcher = URL_PATTERN.matcher(logEntry);
        return matcher.find() ? matcher.group(2) : null;
    }
    private String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return url;
        }
    }
    public Flux<AccessLog> checkPattern(Flux<AccessLog> logs, String[] patterns) {
        return logs.filter(accessLog -> {
            String message = accessLog.getMessage();
            String url = extractUrl(message);
            if (url != null) {
                String decodedUrl = decodeUrl(url);
                for (String pattern : patterns) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(decodedUrl).find()) {
                        // System.out.println("XSS Injection Detected: " + message);
                        return true;
                    }
                }
            }
            return false;
        });
    }
    public Flux<AccessLog> checkDirectoryIndex(Flux<AccessLog> logs) {
        return logs.filter(accessLog -> {
            String message = accessLog.getMessage();
            String url = extractUrl(message);

            if (url != null) {
                String decodedUrl = decodeUrl(url);

                if (decodedUrl.endsWith("/")) {
                    if (!message.contains("404")) {
                        // System.out.println("Directory Index Access Detected without 404: " + message);
                        return true;
                    }
                }

                if (decodedUrl.endsWith("/%00") || decodedUrl.endsWith("/%20")) {
                    // System.out.println("Suspicious URL Access Detected (%00 or %20 in URL): " + message);
                    return true;
                }
            }

            return false;
        });
    }
    public Mono<LocalDateTime> process(LocalDateTime lastProcessedTimestamp) {
        SearchRequest searchRequest = searchRequest(lastProcessedTimestamp);
        return executeSearch(searchRequest)
                .collectList()
                .flatMap(jsonNodes -> {
                    Flux<EventStream> eventStreamFlux = mapJsonToEventStream(jsonNodes);

                    List<AccessLog> accessLogList = jsonNodes.stream()
                            .map(AccessLogsMapper::mapJsonNodeToAccessLogsEvent)
                            .collect(Collectors.toList());

                    LocalDateTime latestTimestamp = getLatestTimestamp(accessLogList, lastProcessedTimestamp);


                    return eventStreamFlux
                            .collectList()
                            .flatMap(eventStreams -> eventRepository.saveAll(eventStreams)
                                    .then(Mono.just(latestTimestamp))
                                    .onErrorMap(e -> new RuntimeException("Error processing Accesslogs: " + e.getMessage(), e))
                            );
                });
    }

    public LocalDateTime getLatestTimestamp(List<AccessLog> accessLogs, LocalDateTime lastProcessedTimestamp) {
        return accessLogs.stream()
                .map(AccessLog::getTimestamp)
                .max(Comparator.naturalOrder())
                .orElse(lastProcessedTimestamp);
    }

    public Flux<EventStream> mapJsonToEventStream(List<JsonNode> jsonNodes) {
        List<AccessLog> accessLogList = jsonNodes.stream()
                .map(AccessLogsMapper::mapJsonNodeToAccessLogsEvent)
                .collect(Collectors.toList());

        Flux<AccessLog> accessLogsFlux = Flux.fromIterable(accessLogList);

        return Flux.merge(
                checkPattern(accessLogsFlux,SQL_INJECTION_PATTERNS)
                        .map(accessLog -> mapLogsToEvent("SQL Injection", "Web", accessLog)),
                checkPattern(accessLogsFlux,XSS_PATTERNS)
                        .map(accessLog -> mapLogsToEvent("XSS", "Web", accessLog)),
                checkPattern(accessLogsFlux,ADMIN_PAGE_PATTERNS)
                        .map(accessLog -> mapLogsToEvent("Admin Page Exposure", "Web", accessLog)),
                checkDirectoryIndex(accessLogsFlux)
                        .map(accessLog -> mapLogsToEvent("Directory Indexing", "Web", accessLog))
        );
    }


    private SearchRequest searchRequest(LocalDateTime timestamp) {

        String timestampString = timestamp.format(DateTimeFormatter.ISO_DATE_TIME);

        Query query = Query.of(q -> q.bool(b -> b
                .must(m -> m.match(t -> t.field("@log_group.keyword").query(FieldValue.of(logGroup))))
                .filter(f -> f.range(r -> r.field("@timestamp").gt(JsonData.of(timestampString))))
        ));

        SourceFilter sourceFilter = SourceFilter.of(s -> s.includes("@id", "@log_group", "@timestamp", "@message"));
        SourceConfig sourceConfig = SourceConfig.of(src -> src.filter(sourceFilter));

        return SearchRequest.of(s -> s.index(indexName)
                .source(sourceConfig)
                .query(query)
                .sort(sort -> sort.field(f -> f.field("@timestamp").order(SortOrder.Asc)))
                .size(30));
    }
}
