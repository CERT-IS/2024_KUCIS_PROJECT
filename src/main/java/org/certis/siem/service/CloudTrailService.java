package org.certis.siem.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.log.AccessLog;
import org.certis.siem.entity.log.CloudTrailLog;
import org.certis.siem.entity.EventStream;
import org.certis.siem.mapper.AccessLogsMapper;
import org.certis.siem.mapper.CloudTrailMapper;
import org.certis.siem.repository.EventRepository;
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

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.certis.siem.mapper.EventMapper.mapLogsToEvent;
import static org.certis.siem.service.OpenSearchService.executeSearch;

@Service
@RequiredArgsConstructor
public class CloudTrailService {

    private final OpenSearchClient openSearchClient;
    private final String indexName = "cwl-*";
    private final String logGroup = "aws-cloudtrail-logs-058264524253-eba56a76";
    private final List<String> awsRegions = Arrays.asList(
            "us-east-1", "us-east-2", "us-west-1", "us-west-2",
            "ap-east-1", "ap-south-1", "ap-northeast-1", "ap-northeast-2",
            "ap-northeast-3", "ap-southeast-1", "ap-southeast-2",
            "ca-central-1", "eu-central-1", "eu-west-1", "eu-west-2",
            "eu-west-3", "eu-north-1", "sa-east-1", "me-south-1",
            "af-south-1", "eu-south-1", "eu-south-2"
    );
    private final EventRepository eventRepository;

    private final List<String> excludeRegions = new ArrayList<>(List.of("ap-northeast-2"));
    private Set<String> whitelistAddresss = new HashSet<>(Arrays.asList());

    private boolean isSuspiciousAddress(String ip) {
        return !whitelistAddresss.contains(ip);
    }

    public Mono<List<String>> getExcludeRegions() {
        return Mono.just(excludeRegions);
    }

    public void addExcludeRegions(List<String> adds) {
        List<String> validRegions = adds.stream()
                .filter(awsRegions::contains)
                .filter(region -> !excludeRegions.contains(region))
                .collect(Collectors.toList());

        excludeRegions.addAll(validRegions);
    }

    public void deleteExcludeRegions(List<String> deletes) {
        List<String> validRegions = deletes.stream()
                .filter(awsRegions::contains)
                .collect(Collectors.toList());

        excludeRegions.removeAll(validRegions);
    }

    public Mono<LocalDateTime> process(LocalDateTime lastProcessedTimestamp) {
        SearchRequest searchRequest = searchRequest(lastProcessedTimestamp);

        return executeSearch(searchRequest)
                .collectList()
                .flatMap(jsonNodes -> {
                    Flux<EventStream> eventStreamFlux = mapJsonNodeToCloudTrailLog(jsonNodes);

                    List<CloudTrailLog> cloudtrailList = jsonNodes.stream()
                            .map(CloudTrailMapper::mapJsonNodeToCloudTrailLog)
                            .collect(Collectors.toList());


                    LocalDateTime latestTimestamp = getLatestTimestamp(cloudtrailList, lastProcessedTimestamp);


                    return eventStreamFlux
                            .collectList()
                            .flatMap(eventStreams -> eventRepository.saveAll(eventStreams)
                                    .then(Mono.just(latestTimestamp))
                                    .onErrorMap(e -> new RuntimeException("Error processing Accesslogs: " + e.getMessage(), e))
                            );
                });
    }

    public LocalDateTime getLatestTimestamp(List<CloudTrailLog> cloudTrailLogs, LocalDateTime lastProcessedTimestamp) {
        return cloudTrailLogs.stream()
                .map(CloudTrailLog::getTimestamp)
                .max(Comparator.naturalOrder())
                .orElse(lastProcessedTimestamp);
    }

    public Flux<EventStream> mapJsonNodeToCloudTrailLog(List<JsonNode> jsonNodes) {
        List<CloudTrailLog> cloudTrailList = jsonNodes.stream()
                .map(CloudTrailMapper::mapJsonNodeToCloudTrailLog)
                .collect(Collectors.toList());

        Flux<CloudTrailLog> cloudTrailLogFlux = Flux.fromIterable(cloudTrailList);

        return Flux.merge(
                getUnAuthLoginByCloudTrailLogs(cloudTrailLogFlux)
                        .map(cloudTrailLog -> mapLogsToEvent("인가받지 않은 로그인 시도", "클라우드", cloudTrailLog)),
                getNotInRegionByCloudTrailLogs(cloudTrailLogFlux)
                        .map(cloudTrailLog -> mapLogsToEvent("지정되지 않은 AWS 리전에서의 접근 시도", "클라우드", cloudTrailLog)),
                getContainsWildcardByCloudTrailLogs(cloudTrailLogFlux)
                        .map(cloudTrailLog -> mapLogsToEvent("IAM 역할 변조를 통한 권한 상승", "클라우드", cloudTrailLog))
        );
    }

    private Flux<CloudTrailLog> getUnAuthLoginByCloudTrailLogs(Flux<CloudTrailLog> logs) {
        return logs
                .filter(this::isFailedLogin)
                .groupBy(CloudTrailLog::getSourceIPAddress)
                .flatMap(groupedFlux -> groupedFlux.collectList()
                        .filter(list -> hasThreeFailuresWithinTimeWindow(list, Duration.ofMinutes(10))))
                .flatMap(Flux::fromIterable);
    }

    private Flux<CloudTrailLog> getNotInRegionByCloudTrailLogs(Flux<CloudTrailLog> logs) {
        return logs.filter(log -> isRegionExcluded(excludeRegions, log));
    }

    private Flux<CloudTrailLog> getContainsWildcardByCloudTrailLogs(Flux<CloudTrailLog> logs){
        return logs.filter(log -> containsWildcard(log));
    }

    private boolean isRegionExcluded(List<String> excludedRegions, CloudTrailLog log) {
        String awsRegion = log.getAwsRegion();
        return !excludedRegions.contains(awsRegion);
    }

    private boolean isFailedLogin(CloudTrailLog log) {
        Map<String, Object> responseElements = log.getResponseElements();
        if (responseElements != null) {
            Object consoleLogin = responseElements.get("ConsoleLogin");
            return "Failure".equals(consoleLogin);
        }
        return false;
    }

    private boolean hasThreeFailuresWithinTimeWindow(List<CloudTrailLog> logs, Duration timeWindow) {
        if (logs.size() < 3) {
            return false;
        }

        logs.sort(Comparator.comparing(CloudTrailLog::getEventTime));

        for (int i = 0; i <= logs.size() - 3; i++) {
            Instant time1 = logs.get(i).getEventTime();
            Instant time3 = logs.get(i + 2).getEventTime();

            if (Duration.between(time1, time3).compareTo(timeWindow) <= 0) {
                return true;
            }
        }

        return false;
    }

    private boolean containsWildcard(CloudTrailLog log){
        Map<String, Object> requestParameters = log.getRequestParameters();

        if(requestParameters == null)
            return false;

        Object policyDocument = requestParameters.get("policyDocument");
        if (policyDocument != null && policyDocument instanceof String) {
            String policyDoc = (String) policyDocument;
            return policyDoc.contains("\"Action\": \"*\"") || policyDoc.contains("\"Resource\": \"*\"");
        }

        return false;
    }


    private SearchRequest searchRequest(LocalDateTime timestamp) {
        String timestampString = timestamp.format(DateTimeFormatter.ISO_DATE_TIME);

        Query query = Query.of(q -> q.bool(b -> b
                .must(m -> m.match(t -> t.field("@log_group.keyword").query(FieldValue.of(logGroup))))
                .filter(f -> f.range(r -> r.field("@timestamp").gt(JsonData.of(timestampString))))
        ));

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes(
                        "@id", "@log_group", "@timestamp",
                        "eventID",
                        "eventType",
                        "userIdentity",
                        "eventSource",
                        "eventName",
                        "eventTime",
                        "awsRegion",
                        "sourceIPAddress",
                        "userAgent",
                        "resources",
                        "requestParameters",
                        "responseElements",
                        "additionalEventData",
                        "readOnly"
                )
        );

        SourceConfig sourceConfig = SourceConfig.of(src -> src.filter(sourceFilter));

        return SearchRequest.of(s -> s.index(indexName)
                .source(sourceConfig)
                .query(query)
                .sort(sort -> sort.field(f -> f.field("@timestamp").order(SortOrder.Asc)))
                .size(100));
    }
}