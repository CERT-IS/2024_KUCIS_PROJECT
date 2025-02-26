package org.certis.siem.service;

import static org.certis.siem.mapper.EventMapper.mapLogsToEvent;
import static org.certis.siem.service.OpenSearchService.getSearchQuery;
import static org.certis.siem.service.OpenSearchService.getSearchSort;
import static org.certis.siem.service.OpenSearchService.searchRequestSize;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.log.HttpLog;
import org.certis.siem.mapper.HttpMapper;
import org.certis.siem.repository.EventRepository;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class HttpLogsService {

    private final String HTTPLOG_PROCESS_ERROR_MESSAGE = "Error processing Http Logs: ";

    private final OpenSearchService openSearchService;
    private final EventRepository eventRepository;

    private final String indexName = "cwl-*";
    private final String logGroup = "/aws/lambda/http-gateway";

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

    public String extractUrl(String logEntry) {
        Matcher matcher = URL_PATTERN.matcher(logEntry);
        return matcher.find() ? matcher.group(2) : null;
    }

    public String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return url;
        }
    }
    public Flux<HttpLog> checkPattern(Flux<HttpLog> logs, String[] patterns) {
        return logs.filter(httpLog -> {
            String message = httpLog.getMessage();
            String url = extractUrl(message);
            if (url != null) {
                String decodedUrl = decodeUrl(url);
                for (String pattern : patterns) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(decodedUrl).find()) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    public Flux<HttpLog> checkDirectoryIndex(Flux<HttpLog> logs) {
        return logs.filter(httpLog -> {
            String message = httpLog.getMessage();
            String url = extractUrl(message);

            if (url != null) {
                String decodedUrl = decodeUrl(url);

                if (decodedUrl.endsWith("/")) {
                    if (!message.contains("404")) {
                        return true;
                    }
                }

                if (decodedUrl.endsWith("/%00") || decodedUrl.endsWith("/%20")) {
                    return true;
                }
            }

            return false;
        });
    }
    public Mono<LocalDateTime> process(LocalDateTime lastProcessedTimestamp) {
        return openSearchService.executeSearch(searchRequest(lastProcessedTimestamp))
                .collectList()
                .flatMap(jsonNodes -> mapJsonToEventStream(jsonNodes).collectList()
                            .flatMap(eventStreams -> updateEvent(eventStreams, jsonNodes, lastProcessedTimestamp)));
    }

    private Mono<LocalDateTime> updateEvent(List<EventStream> eventStreams, List<JsonNode> jsonNodes, LocalDateTime lastProcessedTimestamp){
        return eventRepository.saveAll(eventStreams)
                .then(Mono.just(getLatestTimestamp(getHttpLog(jsonNodes), lastProcessedTimestamp)))
                .onErrorMap(e -> new RuntimeException(HTTPLOG_PROCESS_ERROR_MESSAGE + e.getMessage(), e));
    }

    private List<HttpLog> getHttpLog(List<JsonNode> jsonNodes){
        return jsonNodes.stream()
                .map(HttpMapper::convertJsonNodeToHttpLog)
                .collect(Collectors.toList());
    }


    public LocalDateTime getLatestTimestamp(List<HttpLog> httpLogs, LocalDateTime lastProcessedTimestamp) {
        return httpLogs.stream()
                .map(HttpLog::getTimestamp)
                .max(Comparator.naturalOrder())
                .orElse(lastProcessedTimestamp);
    }

    public Flux<EventStream> mapJsonToEventStream(List<JsonNode> jsonNodes) {
        List<HttpLog> httpLogsList = jsonNodes.stream()
                .map(HttpMapper::convertJsonNodeToHttpLog)
                .collect(Collectors.toList());

        Flux<HttpLog> httpLogFlux = Flux.fromIterable(httpLogsList);

        return Flux.merge(
                checkPattern(httpLogFlux,SQL_INJECTION_PATTERNS)
                        .map(httpLog -> mapLogsToEvent("SQL Injection", "Web", httpLog)),
                checkPattern(httpLogFlux,XSS_PATTERNS)
                        .map(httpLog -> mapLogsToEvent("XSS", "Web", httpLog)),
                checkPattern(httpLogFlux,ADMIN_PAGE_PATTERNS)
                        .map(httpLog -> mapLogsToEvent("Admin Page Exposure", "Web", httpLog)),
                checkDirectoryIndex(httpLogFlux)
                        .map(httpLog -> mapLogsToEvent("Directory Indexing", "Web", httpLog))
        );
    }

    private SearchRequest searchRequest(LocalDateTime timestamp) {
        return SearchRequest.of(s -> s.index(indexName)
                .source(getSearchFilterConfig())
                .query(getSearchQuery(logGroup, timestamp))
                .sort(sort -> getSearchSort(sort))
                .size(searchRequestSize));
    }

    private SourceConfig getSearchFilterConfig(){
        SourceFilter sourceFilter = SourceFilter.of(s -> s.includes("@id", "@log_group", "@timestamp", "@message"));
        return SourceConfig.of(src -> src.filter(sourceFilter));
    }
}
