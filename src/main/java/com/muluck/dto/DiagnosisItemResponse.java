package com.muluck.dto;

import com.muluck.domain.Diagnosis;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record DiagnosisItemResponse(
        UUID diagnosisId,
        String imageUrl,
        String diagnosisType, // "HEALTHY" or "DISEASE"
        String diseaseName,
        String diseaseDescription,
        String cause,
        String confidenceScore, // 54% 같은 형식으로 변환
        String diagnosisDate // yyyy.MM.dd
) {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static DiagnosisItemResponse of(Diagnosis d) {
        String formattedDate = d.getDiagnosisDate() != null ? d.getDiagnosisDate().format(DATE_FORMATTER) : null;
        String formattedConfidence = null;

        if ("DISEASE".equalsIgnoreCase(d.getDiagnosisType()) && d.getDiseaseResult() != null) {
            formattedConfidence = formatConfidence(d.getDiseaseResult().getConfidenceScore());
            return new DiagnosisItemResponse(
                    d.getDiagnosisId(),
                    d.getImageUrl(),
                    d.getDiagnosisType(),
                    d.getDiseaseResult().getDiseaseName(),
                    d.getDiseaseResult().getDiseaseDescription(),
                    d.getDiseaseResult().getCause(),
                    formattedConfidence,
                    formattedDate
            );
        } else if ("HEALTHY".equalsIgnoreCase(d.getDiagnosisType()) && d.getHealthyResult() != null) {
            formattedConfidence = formatConfidence(d.getHealthyResult().getConfidenceScore());
            return new DiagnosisItemResponse(
                    d.getDiagnosisId(),
                    d.getImageUrl(),
                    d.getDiagnosisType(),
                    null,
                    null,
                    null,
                    formattedConfidence,
                    formattedDate
            );
        } else {
            return new DiagnosisItemResponse(
                    d.getDiagnosisId(),
                    d.getImageUrl(),
                    d.getDiagnosisType(),
                    null,
                    null,
                    null,
                    null,
                    formattedDate
            );
        }
    }

    private static String formatConfidence(BigDecimal score) {
        if (score == null) return null;
        return score.multiply(BigDecimal.valueOf(100)).setScale(0, BigDecimal.ROUND_HALF_UP) + "%";
    }
}