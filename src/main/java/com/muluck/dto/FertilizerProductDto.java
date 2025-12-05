package com.muluck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FertilizerProductDto {
    
    private String productName;      // 제품명
    private Integer price;            // 가격
    private String unit;              // 판매처
    private String imageUrl;          // 이미지 URL
    private String description;       // 제품 설명
}
