package org.certis.siem.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
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
