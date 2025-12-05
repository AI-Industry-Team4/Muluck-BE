package com.muluck.dto;

import com.muluck.domain.DiseaseResult;

import java.math.BigDecimal;

public record DiseaseResultDto(
        String diseaseName,
        BigDecimal confidenceScore,
        String description,
        String cause,
        String riskLevel,
        String managementGuide
) {
    public static DiseaseResultDto from(DiseaseResult dr) {
        return new DiseaseResultDto(
                dr.getDiseaseName(),
                dr.getConfidenceScore(),
                dr.getDiseaseDescription(),
                dr.getCause(),
                dr.getRiskLevel(),
                dr.getManagementGuide()
        );
    }
}
