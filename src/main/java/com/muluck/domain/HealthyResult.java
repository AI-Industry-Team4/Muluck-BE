package com.muluck.domain;

import com.muluck.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "healthy_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthyResult extends BaseEntity {

    @Id
    @Column(name = "result_id", columnDefinition = "BINARY(16)")
    private UUID resultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "prevention_method", columnDefinition = "TEXT")
    private String preventionMethod;

    @PrePersist
    public void prePersist() {
        if (this.resultId == null) {
            this.resultId = UUID.randomUUID();
        }
    }

    // 생성자
    public HealthyResult(Diagnosis diagnosis, BigDecimal confidenceScore, String preventionMethod) {
        this.diagnosis = diagnosis;
        this.confidenceScore = confidenceScore;
        this.preventionMethod = preventionMethod;
    }
}
