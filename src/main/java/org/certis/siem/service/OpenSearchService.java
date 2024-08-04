package org.certis.siem.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenSearchService {

    private final OpenSearchClient openSearchClient;
    private final String indexName = "cwl-*";

    private static final Pattern URL_PATTERN = Pattern.compile("\"(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH)\\s+(http[^\"]+)\\s+HTTP/1\\.1\"");

    private final String[] SQL_INJECTION_PATTERNS = {
            "(?i)\\b(SELECT|UPDATE|DELETE|INSERT|UNION|DROP|ALTER)\\b",
            "(?i)\\bOR\\b\\s+\\d+=\\d+",
            "(?i)\\bOR\\b\\s+[\\w'\"=]+",
            "(--|#|;)",
            "(?i)\\bUNION\\b\\s*\\bSELECT\\b"
    };


    private final String[] XSS_PATTERNS = {
            "<script.*?>.*?</script>",
            "javascript:",
            "on\\w+=['\"].*?['\"]",
            "<.*?javascript:.*?>",
            "<.*?on\\w+=.*?>"
    };

    private final List<String> awsRegions = Arrays.asList(
            "us-east-1", "us-east-2", "us-west-1", "us-west-2",
            "ap-east-1", "ap-south-1", "ap-northeast-1", "ap-northeast-2",
            "ap-northeast-3", "ap-southeast-1", "ap-southeast-2",
            "ca-central-1", "eu-central-1", "eu-west-1", "eu-west-2",
            "eu-west-3", "eu-north-1", "sa-east-1", "me-south-1",
            "af-south-1", "eu-south-1", "eu-south-2"
    );


    public Flux<JsonNode> getDocumentsByLogGroup(String logGroup) {
//        Query rangeQuery = Query.of(q -> q
//                .range(r -> r
//                        .field("date")
//                        .gte(JsonData.of(startDate))
//                        .lte(JsonData.of(endDate))
//                )
//        );
        Query query = Query.of(q -> q
                .match(t -> t
                        .field("@log_group")
                        .query(FieldValue.of(logGroup))
                )
        );

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id","@log_group", "@timestamp","@message")
        );

        SourceConfig sourceConfig = SourceConfig.of(src -> src
                .filter(sourceFilter)
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .source(sourceConfig)
                .query(query)
                .size(1000) //default = 10
        );

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> {
                    return Flux.fromIterable(searchResponse.hits().hits())
                            .map(hit -> hit.source());
                })
                .onErrorMap(e -> new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e));
    }

    public Flux<JsonNode> getDocumentsByAccessLogs() {
        String logGroup = "/aws/alb/access-logs";
        Query query = Query.of(q -> q
                .match(t -> t
                        .field("@log_group")
                        .query(FieldValue.of(logGroup))
                )
        );

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id","@log_group", "@timestamp","@message")
        );

        SourceConfig sourceConfig = SourceConfig.of(src -> src
                .filter(sourceFilter)
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .source(sourceConfig)
                .query(query)
                .size(100)
        );

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> {
                    return Flux.fromIterable(searchResponse.hits().hits())
                            .map(hit -> hit.source());
                })
                .onErrorMap(e -> new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e));
    }

    private String extractUrl(String logEntry) {
        Matcher matcher = URL_PATTERN.matcher(logEntry);
        if (matcher.find()) {
            String url = matcher.group(2);
            return url;
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

    public Flux<JsonNode> checkSQLInjection(Flux<JsonNode> logs) {
        return logs.filter(logEntry -> {
            String message = logEntry.get("@message").asText();
            String url = extractUrl(message);
            if (url != null) {
                String decodedUrl = decodeUrl(url);
                for (String pattern : SQL_INJECTION_PATTERNS) {
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(decodedUrl).find()) {
                        System.out.println("SQL Injection Detected: " + message);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    public Flux<JsonNode> checkXSS(Flux<JsonNode> logs) {
        return logs.filter(logEntry -> {
            String message = logEntry.get("@message").asText();
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

    public Flux<JsonNode> processAccessLogs() {
        return getDocumentsByAccessLogs()
                .collectList()
                .flatMapMany(logs -> {
                    Flux<JsonNode> sqlInjectionResults = checkSQLInjection(Flux.fromIterable(logs));
                    Flux<JsonNode> xssResults = checkXSS(Flux.fromIterable(logs));

                    return Flux.merge(sqlInjectionResults, xssResults);
                });
    }

    public Flux<JsonNode> getUnAuthLoginByCloudTrailLogs() {
        String logGroup = "aws-cloudtrail-logs-058264524253-eba56a76";
        Query query = Query.of(q -> q
                .match(t -> t
                        .field("@log_group")
                        .query(FieldValue.of(logGroup))
                )
        );

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id", "@log_group", "@timestamp","@message")
        );

        SourceConfig sourceConfig = SourceConfig.of(src -> src
                .filter(sourceFilter)
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .source(sourceConfig)
                .query(query)
                .size(100)
        );

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> {
                    return Flux.fromIterable(searchResponse.hits().hits())
                            .map(hit -> hit.source());
                })
                .onErrorMap(e -> new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e))
                .filter(this::isFailedLogin)
                .groupBy(this::getSourceIPAddress)
                .flatMap(groupedFlux -> groupedFlux.collectList()
                        .filter(list -> hasThreeFailuresWithinTimeWindow(list, Duration.ofMinutes(10))))
                .flatMapIterable(list -> list);
    }

    public Flux<JsonNode> getNotInRegionByCloudTrailLogs(List<String> excludedRegions) {
        String logGroup = "aws-cloudtrail-logs-058264524253-eba56a76";
        Query query = Query.of(q -> q
                .match(t -> t
                        .field("@log_group")
                        .query(FieldValue.of(logGroup))
                )
        );

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id", "@log_group", "@timestamp","@message")
        );

        SourceConfig sourceConfig = SourceConfig.of(src -> src
                .filter(sourceFilter)
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .source(sourceConfig)
                .query(query)
                .size(100)
        );

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> {
                    return Flux.fromIterable(searchResponse.hits().hits())
                            .map(hit -> hit.source());
                })
                .onErrorMap(e -> new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e))
                .filter(log -> isRegionExcluded(excludedRegions, log));
    }
    private boolean isRegionExcluded(List<String> excludedRegions, JsonNode log) {
        String awsRegion = log.path("awsRegion").asText();
        return !excludedRegions.contains(awsRegion);
    }

    private boolean isFailedLogin(JsonNode log) {
        return "Failure".equals(log.path("responseElements").path("ConsoleLogin").asText());
    }

    private String getSourceIPAddress(JsonNode log) {
        return log.path("sourceIPAddress").asText();
    }

    private boolean hasThreeFailuresWithinTimeWindow(List<JsonNode> logs, Duration timeWindow) {
        if (logs.size() < 3) {
            return false;
        }

        logs.sort((log1, log2) -> Instant.parse(log1.path("eventTime").asText())
                .compareTo(Instant.parse(log2.path("eventTime").asText())));

        for (int i = 0; i <= logs.size() - 3; i++) {
            Instant time1 = Instant.parse(logs.get(i).path("eventTime").asText());
            Instant time3 = Instant.parse(logs.get(i + 2).path("eventTime").asText());

            if (Duration.between(time1, time3).compareTo(timeWindow) <= 0) {
                return true;
            }
        }

        return false;
    }

    public Mono<List<JsonNode>> checkFieldExistence(String fieldName) {
        Query query = Query.of(q -> q
                .exists(e -> e.field(fieldName))
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(query)
        );

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .map(searchResponse -> searchResponse.hits().hits().stream()
                        .map(hit -> hit.source())
                        .collect(Collectors.toList()))
                .onErrorMap(e -> new RuntimeException("Error checking field existence in OpenSearch: " + e.getMessage(), e));
    }
}
