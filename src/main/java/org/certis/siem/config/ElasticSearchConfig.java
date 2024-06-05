package org.certis.siem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;


@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = "org.certis.siem.repository")
public class ElasticSearchConfig extends ElasticsearchConfiguration {

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo("elasticsearch:9200")
                .build();
    }
}
