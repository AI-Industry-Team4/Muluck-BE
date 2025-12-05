package com.muluck.dto;

import com.muluck.domain.Diagnosis;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiagnosisDetailResponse(
        UUID diagnosisId,
        String imageUrl,
        String diagnosisType,
        LocalDateTime diagnosisDate,
        Object result // DiseaseResultDto 또는 HealthyResultDto
) {

    public static DiagnosisDetailResponse from(Diagnosis diagnosis) {

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
                diagnosis.getDiagnosisDate(),
                resultData
        );
    }
}

