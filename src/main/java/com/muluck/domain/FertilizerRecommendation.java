package com.muluck.domain;

import com.muluck.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "fertilizer_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FertilizerRecommendation extends BaseEntity {

    @Id
    @Column(name = "recommendation_id", columnDefinition = "BINARY(16)")
    private UUID recommendationId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "product_url", length = 500)
    private String productUrl;

    @PrePersist
    public void prePersist() {
        if (this.recommendationId == null) {
            this.recommendationId = UUID.randomUUID();
        }
    }

    // 생성자
    public FertilizerRecommendation(String productName, String productDescription, String productUrl) {
        this.productName = productName;
        this.productDescription = productDescription;
        this.productUrl = productUrl;
    }

    // 비즈니스 메서드
    public void updateProduct(String productName, String productDescription, String productUrl) {
        this.productName = productName;
        this.productDescription = productDescription;
        this.productUrl = productUrl;
    }
}
