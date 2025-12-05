package com.muluck.service;

import com.muluck.dto.FertilizerProductDto;
import com.muluck.repository.FertilizerRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FertilizerService {

    private final FertilizerRecommendationRepository fertilizerRecommendationRepository;

    // 추천 비료 제품 목록 조회
    public List<FertilizerProductDto> getRecommendedProducts() {
        return Arrays.asList(
            FertilizerProductDto.builder()
                .productName("화분 화초 식물영양제 액체 비료")
                .price(2300)
                .unit("공구명가")
                .imageUrl("https://muluckstorage.blob.core.windows.net/muluck-container/화분 화초 식물영양제 액체 비료.png")
                .description("액상 비료로, 물 주듯 간편하게 사용 가능. 화분, 실내 식물 적합.")
                .build(),
            
            FertilizerProductDto.builder()
                .productName("복합 비료 화분 식물영양제")
                .price(15630)
                .unit("SSG.COM")
                .imageUrl("https://muluckstorage.blob.core.windows.net/muluck-container/복합 비료 화분 식물영양제.png")
                .description("여러 영양소 균형 있게 포함된 복합 비료. 화분, 텃밭 모두 무난.")
                .build(),
            
            FertilizerProductDto.builder()
                .productName("화분백화점 미농 부숙비료")
                .price(10230)
                .unit("SSG.COM")
                .imageUrl("https://muluckstorage.blob.core.windows.net/muluck-container/화분백화점 미농 부숙비료.png")
                .description("퇴비형 비료. 토양 개량 + 장기적인 영양 공급 위한 선택지.")
                .build(),

                FertilizerProductDto.builder()
                        .productName("바이오랜드 화분용 천연칼슘 비료 500g")
                        .price(4500)
                        .unit("쿠팡")
                        .imageUrl("https://muluckstorage.blob.core.windows.net/muluck-container/바이오 계분 비료.png")
                        .description("화분용, 칼슘 포함 비료. 꽃이나 채소, 화분 식물에 칼슘 보충용.")
                        .build()
        );
    }
}
