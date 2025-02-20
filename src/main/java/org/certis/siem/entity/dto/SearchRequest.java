package org.certis.siem.entity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
    private String newIndex;
    private String logGroup;
    private String startDate;
    private String endDate;
    private String whereClause;
    private List<String> fields;
}
