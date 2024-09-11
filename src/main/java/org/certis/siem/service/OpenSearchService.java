package org.certis.siem.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.WAFLog;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.utils.WAFMapper;
import org.certis.siem.utils.EventMapper;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.SourceConfig;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenSearchService {

    private final OpenSearchClient openSearchClient;
    private final EventRepository eventRepository;

    private final String indexName = "cwl-*";
    private final String logGroup = "aws-waf-logs-groups";

    public Mono<LocalDateTime> processWAFLogs(LocalDateTime lastProcessedTimestamp) {
        return execeSearch(lastProcessedTimestamp)
                .collectList()
                .flatMap(logs -> {
                    Flux<WAFLog> wafLogs = Flux.fromIterable(logs)
                            .map(WAFMapper::mapJsonNodeToWAFEvent);

                    return wafLogs.collectList()
                            .doOnNext(wafLogList -> System.out.println("WAF Logs: " + wafLogList))
                            .flatMap(wafLogList -> {
                                Flux<EventStream> wafResults = Flux.fromIterable(wafLogList)
                                        .map(wafLog -> EventMapper.mapWAFLogsToEvent("WAF EventStream", "WAF", wafLog));

                                return wafResults.collectList()
                                        .flatMap(eventStreams -> {
                                            Optional<EventStream> latestEventOptional = eventStreams.stream()
                                                    .max(Comparator.comparing(EventStream::getTimestamp));

                                            if (latestEventOptional.isPresent()) {
                                                EventStream latestEvent = latestEventOptional.get();
                                                LocalDateTime latestTimestamp = latestEvent.getTimestamp();
                                                return eventRepository.saveAll(eventStreams)
                                                        .then(Mono.just(latestTimestamp));
                                            } else {
                                                return Mono.empty();
                                            }
                                        });
                            });
                })
                .onErrorMap(e -> new RuntimeException("Error processing WAF logs: " + e.getMessage(), e));
    }


    public Flux<JsonNode> execeSearch(LocalDateTime lastProcessedTimestamp) {
        Query query = Query.of(q -> q.bool(b -> b
                .must(m -> m.match(t -> t
                        .field("@log_group")
                        .query(FieldValue.of(logGroup))))
                .filter(f -> f.range(r -> r
                        .field("@timestamp")
                        .gte(JsonData.of(lastProcessedTimestamp.toString()))))));

        SourceFilter sourceFilter = SourceFilter.of(s -> s
                .includes("@id", "@log_group", "@timestamp", "terminatingRuleType", "terminatingRuleId", "action", "httpRequest.country", "httpRequest.clientIp"));

        SourceConfig sourceConfig = SourceConfig.of(src -> src.filter(sourceFilter));

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .source(sourceConfig)
                .query(query)
                .sort(sort-> sort.field(f -> f.field("@timestamp")
                        .order(SortOrder.Desc)))
                .size(30));

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> Flux.fromIterable(searchResponse.hits().hits())
                        .map(hit -> hit.source()))
                .onErrorMap(e -> new RuntimeException("Error retrieving documents from OpenSearch: " + e.getMessage(), e));
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

    public Flux<JsonNode> executeQueryStringSearch(String queryString) {
        QueryStringQuery queryStringQuery = new QueryStringQuery.Builder()
                .query(queryString)
                .defaultField("@message")
                .build();

        Query query = Query.of(q -> q.queryString(queryStringQuery));

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(query));

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> Flux.fromIterable(searchResponse.hits().hits())
                        .map(hit -> hit.source()))
                .onErrorMap(e -> new RuntimeException("Error executing query string search in OpenSearch: " + e.getMessage(), e));
    }

    public Flux<JsonNode> executeConditionalSearch(String newIndex, String newGroup, String whereClause, String startDate, String endDate, List<String> fields, int size) {
        Query query = Query.of(q -> q.bool(b -> b
                .must(mustQuery -> mustQuery
                        .match(t -> t
                                .field("@log_group")
                                .query(FieldValue.of(newGroup))
                        )
                )
                .filter(f -> f
                        .range(r -> r
                                .field("@timestamp")
                                .gte(JsonData.of(startDate))
                                .lte(JsonData.of(endDate))
                        )
                )
        ));

//        {
//            "newIndex": "cwl-*",
//                "logGroup": "aws-waf-logs-groups",
//                "startDate": "2024-08-03T17:36:21.164",
//                "endDate": "2024-08-04T23:43:09.545",
//                "whereClause": "string",
//                "fields": [ "@timestamp","@message" ]
//        }

        SourceFilter sourceFilter;
        if (fields != null && !fields.isEmpty()) {
            System.out.println("fields is not empty. "+fields);
            sourceFilter = SourceFilter.of(s -> s.includes(fields));
        } else {
            sourceFilter = SourceFilter.of(s -> s.includes("*"));
        }

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(newIndex)
                .source(src -> src.filter(sourceFilter))
                .query(query)
                .size(size)
        );

        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> Flux.fromIterable(searchResponse.hits().hits())
                        .map(hit -> hit.source())
                )
                .onErrorMap(e -> new RuntimeException("OpenSearch에서 문서를 검색하는 중 오류 발생: " + e.getMessage(), e));
    }

}
