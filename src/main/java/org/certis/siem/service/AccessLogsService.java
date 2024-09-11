package org.certis.siem.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.AccessLog;
import org.certis.siem.entity.EventStream;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.utils.AccessLogsMapper;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.certis.siem.utils.EventMapper.mapAccessLogsToEvent;

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
    public Flux<AccessLog> checkSQLInjection(Flux<AccessLog> logs) {
        return logs.filter(accessLog -> {
            String message = accessLog.getMessage();
            String url = extractUrl(message);
            if (url != null) {
                String decodedUrl = decodeUrl(url);
                for (String pattern : SQL_INJECTION_PATTERNS) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(decodedUrl).find()) {
                        // System.out.println("SQL Injection Detected: " + message);
                        return true;
                    }
                }
            }
            return false;
        });
    }
    public Flux<AccessLog> checkXSS(Flux<AccessLog> logs) {
        return logs.filter(accessLog -> {
            String message = accessLog.getMessage();
            String url = extractUrl(message);
            if (url != null) {
                String decodedUrl = decodeUrl(url);
                for (String pattern : XSS_PATTERNS) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(decodedUrl).find()) {
                        // System.out.println("XSS Injection Detected: " + message);
                        return true;
                    }
                }
            }
            return false;
        });
    }
    public Flux<AccessLog> checkAdminPage(Flux<AccessLog> logs) {
        return logs.filter(accessLog -> {
            String message = accessLog.getMessage();
            String url = extractUrl(message);
            if (url != null) {
                String decodedUrl = decodeUrl(url);
                for (String pattern : ADMIN_PAGE_PATTERNS) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(decodedUrl).find()) {
                        // System.out.println("Admin Page Detected: " + message);
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

    public Mono<LocalDateTime> processAccessLogs(LocalDateTime lastProcessedTimestamp) {
        System.out.println("AccessLogs execute Search " + lastProcessedTimestamp);
        return executeSearch(lastProcessedTimestamp)
                .collectList()
                .doOnNext(waf -> System.out.println("AccessLogs: " + waf))
                .flatMap(logs -> {
                    // Convert JSON logs to AccessLog objects
                    List<AccessLog> accessLogList = logs.stream()
                            .map(AccessLogsMapper::mapJsonNodeToAccessLogsEvent)
                            .collect(Collectors.toList());

                    // Determine the latest timestamp from the list of AccessLog objects
                    LocalDateTime latestTimestamp = accessLogList.stream()
                            .map(AccessLog::getTimestamp)
                            .max(Comparator.naturalOrder())
                            .orElse(lastProcessedTimestamp);

                    System.out.println("timestamp last : "+latestTimestamp);
                    // Create Flux from the list for further processing
                    Flux<AccessLog> accessLogsFlux = Flux.fromIterable(accessLogList);

                    // Perform security checks on the list of AccessLog objects
                    Flux<EventStream> sqlInjectionResults = checkSQLInjection(accessLogsFlux)
                            .map(accessLog -> mapAccessLogsToEvent("SQL Injection", "Web", accessLog));

                    Flux<EventStream> xssResults = checkXSS(accessLogsFlux)
                            .map(accessLog -> mapAccessLogsToEvent("XSS", "Web", accessLog));

                    Flux<EventStream> adminResults = checkAdminPage(accessLogsFlux)
                            .map(accessLog -> mapAccessLogsToEvent("Admin Page Exposure", "Web", accessLog));

                    Flux<EventStream> directoryResults = checkDirectoryIndex(accessLogsFlux)
                            .map(accessLog -> mapAccessLogsToEvent("Directory Indexing", "Web", accessLog));

                    // Merge results and save them
                    return Flux.merge(sqlInjectionResults, xssResults, adminResults, directoryResults)
                            .collectList()
                            .flatMap(eventStreams -> eventRepository.saveAll(eventStreams)
                                    .then(Mono.just(latestTimestamp))
                                    .onErrorMap(e -> new RuntimeException("Error processing Accesslogs: " + e.getMessage(), e))
                            );
                });
    }


    private Flux<JsonNode> executeSearch(LocalDateTime lastProcessedTimestamp) {
        Query query = Query.of(q -> q.bool(b -> b
                .must(m -> m.match(t -> t
                        .field("@log_group")
                        .query(FieldValue.of(logGroup))))
                .filter(f -> f.range(r -> r
                        .field("@timestamp")
                        .gte(JsonData.of(lastProcessedTimestamp.toString()))))));

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id", "@log_group", "@timestamp", "@message")
        );

        SourceConfig sourceConfig = SourceConfig.of(src -> src.filter(sourceFilter));

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .source(sourceConfig)
                .query(query)
                .sort(sort-> sort.field(f -> f.field("@timestamp")
                        .order(SortOrder.Desc)))
                .size(30)
        );

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> Flux.fromIterable(searchResponse.hits().hits())
                        .map(hit -> hit.source()))
                .onErrorMap(e -> new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e));
    }

}
