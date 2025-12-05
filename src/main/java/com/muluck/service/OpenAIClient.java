package com.muluck.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muluck.domain.CaseType;
import com.muluck.dto.diagnosis.GptPromptRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    // GPT-4o-mini를 호출하여 병충해 정보를 생성
    public GptPromptRequestDto.DiseaseCandidate analyzePrimaryDisease(GptPromptRequestDto promptRequest) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(promptRequest);

            String gptResponse = callGptApi(systemPrompt, userPrompt);
            
            // 전체 JSON을 GptPromptRequestDto로 파싱
            GptPromptRequestDto parsedResponse = objectMapper.readValue(gptResponse, GptPromptRequestDto.class);
            
            // primaryDisease 추출
            return parsedResponse.getPrimaryDisease();

        } catch (Exception e) {
            log.error("Failed to analyze disease with GPT", e);
            throw new RuntimeException("병충해 분석에 실패했습니다.", e);
        }
    }

    // GPT-4o-mini를 호출하여 건강한 식물 관리 팁 생성
    public List<String> generateHealthyCareTips(String crop) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildHealthyPrompt(crop);

            String gptResponse = callGptApi(systemPrompt, userPrompt);
            
            // JSON 응답 파싱
            JsonNode jsonNode = objectMapper.readTree(gptResponse);
            return objectMapper.convertValue(
                jsonNode.get("careTips"), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

        } catch (Exception e) {
            log.error("Failed to generate care tips with GPT", e);
            throw new RuntimeException("관리 팁 생성에 실패했습니다.", e);
        }
    }

    // GPT API 호출
    private String callGptApi(String systemPrompt, String userPrompt) {
        try {
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 요청 본문 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            // 응답에서 content 추출
            JsonNode responseNode = objectMapper.readTree(response.getBody());
            String content = responseNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.info("GPT API call successful");
            return content;

        } catch (Exception e) {
            log.error("Failed to call OpenAI API", e);
            throw new RuntimeException("GPT API 호출에 실패했습니다.", e);
        }
    }

    // System 프롬프트 생성
    private String buildSystemPrompt() {
        return """
            당신은 주말농장 사용자들을 위한 친절한 식물 병해충 전문가입니다.
            
            사용자는 사과, 포도, 토마토, 옥수수, 감자, 피망 등의 작물을 키우고 있으며,
            AI가 예측한 병해충 결과를 이해하기 쉽게 설명해 주는 것이 당신의 역할입니다.
            
            규칙:
            1. 출력은 항상 유효한 JSON 형식이어야 합니다.
            2. JSON 이외의 설명, 문장, 공백 줄을 추가로 출력하지 마세요.
            3. 입력으로 제공된 JSON의 구조와 필드 이름을 그대로 유지하세요.
            4. description, causes, careTips처럼 비어 있는 필드만 한국어로 채워 넣으세요.
            5. causes 배열의 각 원소는 "~가 원인일 수 있습니다."로 끝나야 합니다.
            6. careTips 배열의 각 원소는 "~ 하기"로 시작해야 합니다.
            7. 모든 문자열은 쌍따옴표(")를 사용하고, JSON 문법을 반드시 지키세요.
            """;
    }

    // User 프롬프트 생성 (병충해)
    private String buildUserPrompt(GptPromptRequestDto promptRequest) {
        CaseType caseType = promptRequest.getCaseType();
        
        try {
            String inputJson = objectMapper.writeValueAsString(promptRequest);
            
            return switch (caseType) {
                case CERTAIN_DISEASE -> """
                    아래 JSON은 이미지 진단 모델이 예측한 결과입니다.
                    
                    primaryDisease.description, primaryDisease.causes, primaryDisease.careTips를 한국어로 채워 넣으세요.
                    
                    입력 JSON:
                    %s
                    
                    요구사항:
                    1. primaryDisease.description에는 해당 질병에 대한 간단한 설명을 2~3문장으로 작성하세요.
                    2. primaryDisease.causes는 길이 3의 배열로 작성하고, 각 원소는 "~가 원인일 수 있습니다."로 끝나는 문장으로 작성하세요.
                    3. primaryDisease.careTips는 길이 3의 배열로 작성하고, 각 원소는 "~ 하기"로 시작하는 관리 방법으로 작성하세요.
                    """.formatted(inputJson);
                    
                case CANDIDATES_3 -> """
                    아래 JSON은 이미지 진단 모델이 예측한 결과입니다.
                    
                    각 후보(candidates[*])에 대해 description을 2~3문장으로 작성하고,
                    primaryDisease.description, primaryDisease.causes, primaryDisease.careTips도 채워 넣으세요.
                    
                    입력 JSON:
                    %s
                    
                    요구사항:
                    1. 각 후보의 description은 초보자가 쉽게 이해할 수 있게 작성합니다.
                    2. primaryDisease.causes: 길이 3 배열, 각 문장은 "~가 원인일 수 있습니다."로 끝나야 합니다.
                    3. primaryDisease.careTips: 길이 3 배열, 각 항목은 "~ 하기"로 시작해야 합니다.
                    """.formatted(inputJson);
                    
                case CANDIDATES_2_RETAKE -> """
                    아래 JSON은 이미지 진단 모델이 예측한 결과입니다.
                    
                    각 후보(candidates[*])에 대해 description을 2~3문장으로 작성하세요.
                    
                    입력 JSON:
                    %s
                    """.formatted(inputJson);
                    
                default -> throw new IllegalArgumentException("Unsupported case type: " + caseType);
            };
        } catch (Exception e) {
            log.error("Failed to build user prompt", e);
            throw new RuntimeException("프롬프트 생성에 실패했습니다.", e);
        }
    }

    // User 프롬프트 생성 (건강한 식물)
    private String buildHealthyPrompt(String crop) {
        return """
            작물 "%s"에 대한 병충해 예방 관리 팁을 생성해주세요.
            
            JSON 형식:
            {
                "careTips": [
                    "~ 하기",
                    "~ 하기",
                    "~ 하기"
                ]
            }
            
            요구사항:
            1. careTips는 길이 3의 문자열 배열이어야 합니다.
            2. 각 항목은 병충해를 예방하기 위한 관리 팁입니다.
            3. 각 항목은 "~ 하기"로 시작해야 합니다.
            4. JSON만 출력하고 다른 설명은 추가하지 마세요.
            """.formatted(crop);
    }
}
