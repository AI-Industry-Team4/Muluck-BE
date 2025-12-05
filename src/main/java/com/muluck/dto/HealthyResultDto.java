package com.muluck.dto;

import com.muluck.domain.HealthyResult;

import java.math.BigDecimal;

public record HealthyResultDto(
        BigDecimal confidenceScore,
        String preventionMethod
) {
    public static HealthyResultDto from(HealthyResult hr) {
        return new HealthyResultDto(
                hr.getConfidenceScore(),
                hr.getPreventionMethod()
        );
    }
}
