package org.certis.siem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.certis.siem.service.OpenSearchService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class OpenSearchServiceTest {

    @Mock private OpenSearchClient openSearchClient;

    @InjectMocks private OpenSearchService openSearchService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void 쿼리문_실행_메서드() throws Exception {
        // given
        JsonNode mockJsonNode = objectMapper.readTree("{\"field\":\"exists\"}");
        Hit<JsonNode> hit = new Hit.Builder<JsonNode>().source(mockJsonNode).build();
        SearchResponse<JsonNode> mockResponse = new SearchResponse.Builder<JsonNode>()
                .hits(h -> h.hits(Collections.singletonList(hit)))
                .build();

        when(openSearchClient.search(any(SearchRequest.class), eq(JsonNode.class))).thenReturn(mockResponse);

        // when
        Flux<JsonNode> result = openSearchService.executeSearch(null);

        // then
        StepVerifier.create(result)
                .expectNext(mockJsonNode)
                .verifyComplete();
    }

    @Test
    void 해당_필드값이_인덱스에_존재하는지_확인하는_메서드() throws Exception {
        // given
        JsonNode mockJsonNode = objectMapper.readTree("{\"query\":\"match\"}");
        Hit<JsonNode> hit = new Hit.Builder<JsonNode>().source(mockJsonNode).build();

        SearchResponse<JsonNode> mockResponse = new SearchResponse.Builder<JsonNode>()
                .hits(h -> h.hits(Collections.singletonList(hit)))
                .build();

        when(openSearchClient.search(any(SearchRequest.class), eq(JsonNode.class))).thenReturn(mockResponse);


        // when
        Mono<List<JsonNode>> result = openSearchService.checkFieldExistence("testField");

        // then
        StepVerifier.create(result)
                .expectNext(Collections.singletonList(mockJsonNode))
                .verifyComplete();
    }

    @Test
    void QueryString_형태로_로그를_분석하는_메서드() throws Exception {
        // given
        JsonNode mockJsonNode = objectMapper.readTree("{\"query\":\"match\"}");
        Hit<JsonNode> hit = new Hit.Builder<JsonNode>().source(mockJsonNode).build();
        SearchResponse<JsonNode> mockResponse = new SearchResponse.Builder<JsonNode>()
                .hits(h -> h.hits(Collections.singletonList(hit)))
                .build();

        when(openSearchClient.search(any(SearchRequest.class), eq(JsonNode.class))).thenReturn(mockResponse);

        // when
        Flux<JsonNode> result = openSearchService.executeQueryStringSearch("testQuery");

        // then
        StepVerifier.create(result)
                .expectNext(mockJsonNode)
                .verifyComplete();
    }
}
