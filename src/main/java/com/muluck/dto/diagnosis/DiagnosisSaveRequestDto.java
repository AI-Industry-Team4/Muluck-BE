package com.muluck.dto.diagnosis;

import com.muluck.domain.CaseType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class DiagnosisSaveRequestDto {
    
    private String imageUrl;
    private UUID folderId;
    private String imageSource; // "CAMERA" 또는 "GALLERY"
    private CaseType caseType;
    private String crop;
    
    // CERTAIN_DISEASE, CANDIDATES_3에서 사용
    private DiseaseInfo primaryDisease;
    
    // HEALTHY에서 사용
    private Double confidenceScore;
    private List<String> careTips;
    
    @Getter
    @NoArgsConstructor
    public static class DiseaseInfo {
        private String diseaseName;
        private Double confidenceScore;
        private String description;
        private List<String> causes;
        private List<String> managementTips;
    }
}
