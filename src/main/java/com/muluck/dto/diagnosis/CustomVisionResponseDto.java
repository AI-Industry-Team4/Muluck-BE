package com.muluck.dto.diagnosis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomVisionResponseDto {
    
    private String id;
    private String project;
    private String iteration;
    private String created;
    private List<Prediction> predictions;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prediction {
        private Double probability;
        
        @JsonProperty("tagId")
        private String tagId;
        
        @JsonProperty("tagName")
        private String tagName;
    }
}
