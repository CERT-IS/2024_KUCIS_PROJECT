package org.certis.siem.repository;

import org.certis.siem.entity.WAF.WAFEvent;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface WAFRepository extends ReactiveElasticsearchRepository<WAFEvent, String> {
}
