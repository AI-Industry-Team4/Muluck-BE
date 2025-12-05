package com.muluck.dto;

import com.muluck.domain.Diagnosis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record DiagnosisDetailResponse(
        UUID diagnosisId,
        String imageUrl,
        String diagnosisType,
        String diagnosisDate,
        Object result // DiseaseResultDto 또는 HealthyResultDto
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static DiagnosisDetailResponse from(Diagnosis diagnosis) {

        String formattedDate = diagnosis.getDiagnosisDate() != null
                ? diagnosis.getDiagnosisDate().format(DATE_FORMATTER)
                : null;

        Object resultData;

        if ("DISEASE".equals(diagnosis.getDiagnosisType())) {
            resultData = DiseaseResultDto.from(diagnosis.getDiseaseResult());
        } else {
            resultData = HealthyResultDto.from(diagnosis.getHealthyResult());
        }

        return new DiagnosisDetailResponse(
                diagnosis.getDiagnosisId(),
                diagnosis.getImageUrl(),
                diagnosis.getDiagnosisType(),
                formattedDate,
                resultData
        );
    }
}

