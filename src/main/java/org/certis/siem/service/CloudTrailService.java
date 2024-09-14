package org.certis.siem.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.CloudTrailLog;
import org.certis.siem.entity.EventStream;
import org.certis.siem.utils.CloudTrailMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
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

import static org.certis.siem.utils.EventMapper.mapCloudTrailLogsToEvent;
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

    public Flux<EventStream> processCloudTrailLogs() {
        return executeSearch()
                .collectList()
                .flatMapMany(logs -> {
                    Flux<CloudTrailLog> cloudtrailLogs = Flux.fromIterable(logs)
                            .map(CloudTrailMapper::mapJsonNodeToCloudTrailLog);

                    Flux<EventStream> unAuthLoginResults = getUnAuthLoginByCloudTrailLogs(cloudtrailLogs)
                            .map(cloudTrailLog -> mapCloudTrailLogsToEvent("인가받지 않은 로그인 시도", "클라우드", cloudTrailLog));
                    Flux<EventStream> notInRegionResults = getNotInRegionByCloudTrailLogs(cloudtrailLogs)
                            .map(cloudTrailLog -> mapCloudTrailLogsToEvent("지정되지 않은 AWS 리전에서의 접근 시도", "클라우드", cloudTrailLog));

                    return Flux.merge(unAuthLoginResults, notInRegionResults);
                });
    }

    public Flux<CloudTrailLog> getUnAuthLoginByCloudTrailLogs(Flux<CloudTrailLog> logs) {
        return logs
                .filter(this::isFailedLogin)
                .groupBy(CloudTrailLog::getSourceIPAddress)
                .flatMap(groupedFlux -> groupedFlux.collectList()
                        .filter(list -> hasThreeFailuresWithinTimeWindow(list, Duration.ofMinutes(10))))
                .flatMap(Flux::fromIterable);
    }

    public Flux<CloudTrailLog> getNotInRegionByCloudTrailLogs(Flux<CloudTrailLog> logs) {
        return logs.filter(log -> isRegionExcluded(excludeRegions, log));
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

    private Flux<JsonNode> executeSearch() {
        Query query = Query.of(q -> q
                .match(t -> t
                        .field("@log_group")
                        .query(FieldValue.of(logGroup))
                )
        );

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
                .flatMapMany(searchResponse -> Flux.fromIterable(searchResponse.hits().hits())
                        .map(hit -> hit.source()))
                .onErrorMap(e -> new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e));
    }
}