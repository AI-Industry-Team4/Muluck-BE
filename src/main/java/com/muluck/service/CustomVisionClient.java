package com.muluck.service;

import com.muluck.dto.diagnosis.CustomVisionResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomVisionClient {

    private final RestTemplate restTemplate;

    @Value("${azure.custom-vision.prediction-endpoint}")
    private String predictionEndpoint;

    @Value("${azure.custom-vision.prediction-key}")
    private String predictionKey;

    // CustomVision API를 호출하여 이미지 예측 수행
    public CustomVisionResponseDto predict(String imageUrl) {
        try {
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON)); // JSON 응답 요청
            headers.set("Prediction-Key", predictionKey);

            // 요청 본문 생성
            Map<String, String> body = new HashMap<>();
            body.put("url", imageUrl);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            // API 호출
            ResponseEntity<CustomVisionResponseDto> response = restTemplate.exchange(
                    predictionEndpoint,
                    HttpMethod.POST,
                    request,
                    CustomVisionResponseDto.class
            );

            CustomVisionResponseDto result = response.getBody();
            
            if (result != null && result.getPredictions() != null) {
                log.info("CustomVision prediction completed. Predictions count: {}", 
                        result.getPredictions().size());
                return result;
            }

            throw new RuntimeException("CustomVision API returned empty response");

        } catch (Exception e) {
            log.error("Failed to call CustomVision API", e);
            throw new RuntimeException("병충해 예측 API 호출에 실패했습니다.", e);
        }
    }
}
