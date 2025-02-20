package org.certis.siem.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReportRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String template;
    private String description;
    private String reportSource;
    private String notebook;
    private String fileFormat;
    private String reportTrigger;
    private String requestTime;
    private String frequency;
    private int every;
    private String timeUnit;
    private String startTime;
}

