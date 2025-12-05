package com.muluck.dto;

import com.muluck.domain.HealthyResult;

import java.math.BigDecimal;

public record HealthyResultDto(
        String confidenceScore,
        String preventionMethod
) {
    public static HealthyResultDto from(HealthyResult hr) {
        return new HealthyResultDto(
                formatConfidence(hr.getConfidenceScore()),
                hr.getPreventionMethod()
        );
    }

    private static String formatConfidence(BigDecimal score) {
        if (score == null) return null;
        return score.multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP) + "%";
    }
}
