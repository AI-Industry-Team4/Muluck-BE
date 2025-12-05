package com.muluck.domain;

import com.muluck.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "disease_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiseaseResult extends BaseEntity {

    @Id
    @Column(name = "result_id", columnDefinition = "BINARY(16)")
    private UUID resultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(name = "disease_name", nullable = false, length = 100)
    private String diseaseName;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "disease_description", columnDefinition = "TEXT")
    private String diseaseDescription;

    @Column(name = "cause", columnDefinition = "TEXT")
    private String cause;

    @Column(name = "management_guide", columnDefinition = "TEXT")
    private String managementGuide;

    @PrePersist
    public void prePersist() {
        if (this.resultId == null) {
            this.resultId = UUID.randomUUID();
        }
    }

    // 생성자
    public DiseaseResult(Diagnosis diagnosis, String diseaseName, BigDecimal confidenceScore, 
                        String diseaseDescription, String cause, String managementGuide) {
        this.diagnosis = diagnosis;
        this.diseaseName = diseaseName;
        this.confidenceScore = confidenceScore;
        this.diseaseDescription = diseaseDescription;
        this.cause = cause;
        this.managementGuide = managementGuide;
    }
}
