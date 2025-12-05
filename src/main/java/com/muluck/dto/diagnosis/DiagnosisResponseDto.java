package com.muluck.dto.diagnosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.muluck.domain.CaseType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiagnosisResponseDto {
    
    private UUID diagnosisId;
    private String tempDiagnosisId; // 임시 저장용 ID
    private String imageUrl;
    private CaseType caseType;
    private String crop;
    private LocalDateTime diagnosisDate;
    
    // CERTAIN_DISEASE, CANDIDATES_3, CANDIDATES_2_RETAKE에서 사용
    private DiseaseInfo primaryDisease;
    
    // CANDIDATES_3, CANDIDATES_2_RETAKE에서 사용
    private List<DiseaseCandidate> candidates;
    
    // HEALTHY에서 사용
    private Double confidenceScore; // HEALTHY의 confidence score
    private List<String> careTips;
    
    @Getter
    @Builder
    public static class DiseaseInfo {
        private String diseaseName;
        private Double confidenceScore;
        private String description;
        private List<String> causes;
        private List<String> managementTips;
    }
    
    @Getter
    @Builder
    public static class DiseaseCandidate {
        private String diseaseName;
        private Double confidenceScore;
        private Integer rank;
        private String description;
    }
}
