package com.muluck.domain;

import com.muluck.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "diagnosis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diagnosis extends BaseEntity {

    @Id
    @Column(name = "diagnosis_id", columnDefinition = "BINARY(16)")
    private UUID diagnosisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private PlantFolder plantFolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "image_source", nullable = false, length = 20)
    private String imageSource; // "CAMERA" or "GALLERY"

    @Column(name = "diagnosis_type", nullable = false, length = 20)
    private String diagnosisType; // "HEALTHY" or "DISEASE"

    @Column(name = "diagnosis_date", nullable = false)
    private LocalDateTime diagnosisDate;

    @OneToOne(mappedBy = "diagnosis", cascade = CascadeType.ALL, orphanRemoval = true)
    private HealthyResult healthyResult;

    @OneToOne(mappedBy = "diagnosis", cascade = CascadeType.ALL, orphanRemoval = true)
    private DiseaseResult diseaseResult;

    @PrePersist
    public void prePersist() {
        if (this.diagnosisId == null) {
            this.diagnosisId = UUID.randomUUID();
        }
        if (this.diagnosisDate == null) {
            this.diagnosisDate = LocalDateTime.now();
        }
    }

    // 생성자
    public Diagnosis(PlantFolder plantFolder, User user, String imageUrl, String imageSource, String diagnosisType) {
        this.plantFolder = plantFolder;
        this.user = user;
        this.imageUrl = imageUrl;
        this.imageSource = imageSource;
        this.diagnosisType = diagnosisType;
    }

    public void moveToFolder(PlantFolder targetFolder) {
        this.plantFolder = targetFolder;
    }
}
