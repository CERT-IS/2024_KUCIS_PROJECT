package org.certis.siem.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOptions.Builder;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryStringQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.opensearch.client.util.ObjectBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchService {

    public static final Integer searchRequestSize = 1000;
    private final OpenSearchClient openSearchClient;
    private final String indexName = "cwl-*";


    // ---------------------------------------OpenSearch System------------------------------------------//
    public Flux<JsonNode> executeSearch(SearchRequest searchRequest) {
        return Mono.fromCallable(() -> openSearchClient.search(searchRequest, JsonNode.class))
                .flatMapMany(searchResponse -> {
                    if (searchResponse.hits() == null || searchResponse.hits().hits().isEmpty()) {
                        return Flux.error(new RuntimeException("No hits found in OpenSearch response"));
                    }
                    return Flux.fromIterable(searchResponse.hits().hits())
                            .map(hit -> hit.source());
                })
                .onErrorMap(e -> new RuntimeException("Error executing search in OpenSearch", e));
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

    public Flux<JsonNode> executeConditionalSearch(org.certis.siem.entity.dto.SearchRequest request, int size) {
        String newIndex = request.getNewIndex();
        String newGroup = request.getLogGroup();
        String startDate = request.getStartDate();
        String endDate = request.getEndDate();
        List<String> fields = request.getFields();

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

        SourceFilter sourceFilter;
        if (fields != null && !fields.isEmpty()) {
            log.info("fields is not empty. "+fields);
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

    public static Query getSearchQuery(String logGroup, LocalDateTime timestamp){
        String timestampString = timestamp.format(DateTimeFormatter.ISO_DATE_TIME);
        return Query.of(q -> q.bool(b -> b
                .must(m -> m.match(t -> t.field("@log_group.keyword").query(FieldValue.of(logGroup))))
                .filter(f -> f.range(r -> r.field("@timestamp").gt(JsonData.of(timestampString))))
        ));
    }

    public static ObjectBuilder<SortOptions> getSearchSort(Builder sort){
        return sort.field(f -> f.field("@timestamp").order(SortOrder.Asc));
    }
}

//        {
//            "newIndex": "cwl-*",
//                "logGroup": "aws-waf-logs-groups",
//                "startDate": "2024-08-03T17:36:21.164",
//                "endDate": "2024-08-04T23:43:09.545",
//                "whereClause": "string",
//                "fields": [ "@timestamp","@message" ]
//        }