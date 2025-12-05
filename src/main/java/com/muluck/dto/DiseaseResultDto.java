package com.muluck.dto;

import com.muluck.domain.DiseaseResult;

import java.math.BigDecimal;

public record DiseaseResultDto(
        String diseaseName,
        String confidenceScore,
        String description,
        String cause,
        String riskLevel,
        String managementGuide
) {
    public static DiseaseResultDto from(DiseaseResult dr) {
        return new DiseaseResultDto(
                dr.getDiseaseName(),
                formatConfidence(dr.getConfidenceScore()),
                dr.getDiseaseDescription(),
                dr.getCause(),
                dr.getRiskLevel(),
                dr.getManagementGuide()
        );
    }

    private static String formatConfidence(BigDecimal score) {
        if (score == null) return null;
        return score.multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP) + "%";
    }
}
