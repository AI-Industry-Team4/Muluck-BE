package com.muluck.dto.diagnosis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.muluck.domain.CaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GptPromptRequestDto {
    
    private CaseType caseType;
    private String crop;
    
    // CERTAIN_DISEASE에서 사용
    private DiseaseCandidate primaryDisease;
    
    // CANDIDATES_3, CANDIDATES_2_RETAKE에서 사용
    private List<DiseaseCandidate> candidates;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiseaseCandidate {
        private String tag;
        private String crop;
        private String diseaseKo;
        private String diseaseEn;
        private Double probability;
        private Integer probabilityPercent;
        private Integer rank;
        
        // GPT가 채워줄 필드들
        private String description;
        private List<String> causes;
        private List<String> careTips;
    }
}
