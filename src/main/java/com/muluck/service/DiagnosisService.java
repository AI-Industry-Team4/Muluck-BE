package com.muluck.service;

import com.muluck.domain.*;
import com.muluck.dto.diagnosis.*;
import com.muluck.global.exception.BaseException;
import com.muluck.global.exception.ErrorCode;
import com.muluck.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosisService {

    private final BlobStorageService blobStorageService;
    private final CustomVisionClient customVisionClient;
    private final OpenAIClient openAIClient;
    private final DiagnosisRepository diagnosisRepository;
    private final DiseaseResultRepository diseaseResultRepository;
    private final HealthyResultRepository healthyResultRepository;
    private final UserRepository userRepository;
    private final PlantFolderRepository plantFolderRepository;

    // 임시 진단 결과 저장소 (메모리)
    private final Map<String, DiagnosisResponseDto> tempDiagnosisStore = new ConcurrentHashMap<>();

    // 태그명 -> 한국어/영어 병명 매핑
    private static final Map<String, TagMeta> TAG_META_MAP = new HashMap<>();
    
    static {
        // 사과
        TAG_META_MAP.put("apple_scab", new TagMeta("사과", "궤양병", "apple scab", false));
        TAG_META_MAP.put("apple_black", new TagMeta("사과", "검은썩음병", "black rot", false));
        TAG_META_MAP.put("apple_cedar", new TagMeta("사과", "녹병", "cedar apple rust", false));
        TAG_META_MAP.put("apple_healthy", new TagMeta("사과", "건강", "healthy", true));
        
        // 포도
        TAG_META_MAP.put("grape_black", new TagMeta("포도", "검은썩음병", "black rot", false));
        TAG_META_MAP.put("grape_esca", new TagMeta("포도", "줄기마름병", "esca", false));
        TAG_META_MAP.put("grape_leafblight", new TagMeta("포도", "잎마름병", "leaf blight", false));
        TAG_META_MAP.put("grape_healthy", new TagMeta("포도", "건강", "healthy", true));
        
        // 토마토
        TAG_META_MAP.put("tomato_earlyblight", new TagMeta("토마토", "조기역병", "early blight", false));
        TAG_META_MAP.put("tomato_mosaic", new TagMeta("토마토", "모자이크병", "mosaic virus", false));
        TAG_META_MAP.put("tomato_yellowleaf", new TagMeta("토마토", "황화잎말림병", "yellow leaf curl", false));
        TAG_META_MAP.put("tomato_healthy", new TagMeta("토마토", "건강", "healthy", true));
        
        // 옥수수
        TAG_META_MAP.put("corn_cercospora", new TagMeta("옥수수", "회색잎마름병", "cercospora", false));
        TAG_META_MAP.put("corn_common", new TagMeta("옥수수", "일반녹병", "common rust", false));
        TAG_META_MAP.put("corn_northern", new TagMeta("옥수수", "북방잎마름병", "northern leaf blight", false));
        TAG_META_MAP.put("corn_healthy", new TagMeta("옥수수", "건강", "healthy", true));
        
        // 감자
        TAG_META_MAP.put("potato_earlyblight", new TagMeta("감자", "조기역병", "early blight", false));
        TAG_META_MAP.put("potato_lateblight", new TagMeta("감자", "후기역병", "late blight", false));
        TAG_META_MAP.put("potato_healthy", new TagMeta("감자", "건강", "healthy", true));
        
        // 피망
        TAG_META_MAP.put("pepperbell_bacterial", new TagMeta("피망", "세균성점무늬병", "bacterial spot", false));
        TAG_META_MAP.put("pepperbell_healthy", new TagMeta("피망", "건강", "healthy", true));
    }

    // 진단 수행 (저장 안 함)
    @Transactional
    public DiagnosisResponseDto analyze(MultipartFile image, UUID userId) {
        log.info("Starting diagnosis analysis for user: {} (without saving)", userId);
        
        // 1. Blob Storage에 이미지 업로드
        String imageUrl = blobStorageService.uploadImage(image);
        log.info("Image uploaded: {}", imageUrl);
        
        try {
            // 2. CustomVision 호출
            CustomVisionResponseDto cvResponse = customVisionClient.predict(imageUrl);
            
            // 3. CaseType 판단
            CaseType caseType = determineCaseType(cvResponse);
            log.info("Determined case type: {}", caseType);
            
            // 4. 작물명 추출
            String crop = extractCrop(cvResponse);
            
            // 5. 케이스별로 처리
            DiagnosisResponseDto response = switch (caseType) {
                case CERTAIN_DISEASE -> handleCertainDisease(cvResponse, crop, imageUrl);
                case CANDIDATES_3 -> handleCandidates3(cvResponse, crop, imageUrl);
                case CANDIDATES_2_RETAKE -> handleCandidates2Retake(cvResponse, crop, imageUrl);
                case UNDETERMINED_RETAKE -> handleUndeterminedRetake(crop, imageUrl);
                case HEALTHY -> handleHealthy(cvResponse, crop, imageUrl);
            };
            
            // 6. 임시 ID 생성 및 메모리 저장
            String tempId = UUID.randomUUID().toString();
            tempDiagnosisStore.put(tempId, response);
            
            // 7. tempId 포함해서 응답
            response = response.toBuilder()
                    .tempDiagnosisId(tempId)
                    .build();
            
            log.info("Diagnosis analysis completed successfully with tempId: {}", tempId);
            return response;
            
        } catch (Exception e) {
            // 에러 발생 시 업로드된 이미지 삭제
            blobStorageService.deleteImage(imageUrl);
            log.error("Diagnosis analysis failed", e);
            throw new BaseException(ErrorCode.DIAGNOSIS_FAILED);
        }
    }

    // 진단 결과 저장
    @Transactional
    public DiagnosisResponseDto saveDiagnosis(String tempId, UUID folderId, ImageSource imageSource, UUID userId) {
        log.info("Saving diagnosis for user: {}, tempId: {}", userId, tempId);
        
        // 임시 저장소에서 진단 결과 가져오기
        DiagnosisResponseDto tempResponse = tempDiagnosisStore.get(tempId);
        if (tempResponse == null) {
            throw new BaseException(ErrorCode.DIAGNOSIS_NOT_FOUND);
        }
        
        // User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
        
        // PlantFolder 조회 (nullable)
        PlantFolder plantFolder = null;
        if (folderId != null) {
            plantFolder = plantFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BaseException(ErrorCode.PLANT_FOLDER_NOT_FOUND));
        }
        
        // Diagnosis 엔티티 생성
        String diagnosisType = (tempResponse.getCaseType() == CaseType.HEALTHY) ? "HEALTHY" : "DISEASE";
        Diagnosis diagnosis = new Diagnosis(
                plantFolder, 
                user, 
                tempResponse.getImageUrl(), 
                imageSource.name(), // Enum을 String으로 변환
                diagnosisType
        );
        Diagnosis savedDiagnosis = diagnosisRepository.save(diagnosis);
        
        // CaseType별로 결과 저장
        if (tempResponse.getCaseType() == CaseType.HEALTHY) {
            saveHealthyResult(savedDiagnosis, tempResponse);
        } else if (tempResponse.getCaseType() != CaseType.UNDETERMINED_RETAKE 
                && tempResponse.getCaseType() != CaseType.CANDIDATES_2_RETAKE
                && tempResponse.getPrimaryDisease() != null) {
            saveDiseaseResult(savedDiagnosis, tempResponse);
        }
        
        // 임시 저장소에서 제거
        tempDiagnosisStore.remove(tempId);
        
        // 응답 생성
        DiagnosisResponseDto response = tempResponse.toBuilder()
                .diagnosisId(savedDiagnosis.getDiagnosisId())
                .diagnosisDate(savedDiagnosis.getDiagnosisDate())
                .tempDiagnosisId(null) // tempId 제거
                .build();
        
        log.info("Diagnosis saved successfully");
        return response;
    }

    // CaseType 결정
    private CaseType determineCaseType(CustomVisionResponseDto cvResponse) {
        if (cvResponse.getPredictions() == null || cvResponse.getPredictions().isEmpty()) {
            return CaseType.UNDETERMINED_RETAKE;
        }

        var top1 = cvResponse.getPredictions().get(0);
        double p1 = top1.getProbability();
        boolean isHealthy = isHealthyTag(top1.getTagName());

        if (p1 >= 0.6 && isHealthy) {
            return CaseType.HEALTHY;
        }
        if (p1 >= 0.6) {
            return CaseType.CERTAIN_DISEASE;
        }
        if (p1 >= 0.3) {
            return CaseType.CANDIDATES_3;
        }
        if (p1 >= 0.2) {
            return CaseType.CANDIDATES_2_RETAKE;
        }
        return CaseType.UNDETERMINED_RETAKE;
    }

    //  작물명 추출
    private String extractCrop(CustomVisionResponseDto cvResponse) {
        if (cvResponse.getPredictions() == null || cvResponse.getPredictions().isEmpty()) {
            return "알 수 없음";
        }
        
        String topTag = cvResponse.getPredictions().get(0).getTagName();
        TagMeta meta = TAG_META_MAP.get(topTag);
        return meta != null ? meta.crop : "알 수 없음";
    }

    //  CERTAIN_DISEASE 처리
    private DiagnosisResponseDto handleCertainDisease(CustomVisionResponseDto cvResponse, String crop, String imageUrl) {
        var top1 = cvResponse.getPredictions().get(0);
        
        // GPT 프롬프트 요청 생성
        GptPromptRequestDto promptRequest = buildPromptRequest(CaseType.CERTAIN_DISEASE, crop, cvResponse);
        
        // GPT 호출하여 질병 정보 생성
        GptPromptRequestDto.DiseaseCandidate analyzed = openAIClient.analyzePrimaryDisease(promptRequest);
        
        return DiagnosisResponseDto.builder()
                .imageUrl(imageUrl)
                .caseType(CaseType.CERTAIN_DISEASE)
                .crop(crop)
                .primaryDisease(DiagnosisResponseDto.DiseaseInfo.builder()
                        .diseaseName(analyzed.getDiseaseKo())
                        .confidenceScore(analyzed.getProbability())
                        .description(analyzed.getDescription())
                        .causes(analyzed.getCauses())
                        .managementTips(analyzed.getCareTips())
                        .build())
                .build();
    }

    // CANDIDATES_3 처리
    private DiagnosisResponseDto handleCandidates3(CustomVisionResponseDto cvResponse, String crop, String imageUrl) {
        // GPT 프롬프트 요청 생성
        GptPromptRequestDto promptRequest = buildPromptRequest(CaseType.CANDIDATES_3, crop, cvResponse);
        
        // GPT 호출 - 3개 후보 모두 분석
        List<GptPromptRequestDto.DiseaseCandidate> analyzedCandidates = openAIClient.analyzeCandidates(promptRequest);
        
        // 후보 목록 생성 (GPT에서 받은 description 포함)
        List<DiagnosisResponseDto.DiseaseCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < analyzedCandidates.size(); i++) {
            var analyzed = analyzedCandidates.get(i);
            
            candidates.add(DiagnosisResponseDto.DiseaseCandidate.builder()
                    .diseaseName(analyzed.getDiseaseKo())
                    .confidenceScore(analyzed.getProbability())
                    .rank(i + 1)
                    .description(analyzed.getDescription())  // GPT로 생성된 description
                    .build());
        }
        
        // primaryDisease는 첫 번째 후보
        var primaryCandidate = analyzedCandidates.get(0);
        
        return DiagnosisResponseDto.builder()
                .imageUrl(imageUrl)
                .caseType(CaseType.CANDIDATES_3)
                .crop(crop)
                .candidates(candidates)
                .primaryDisease(DiagnosisResponseDto.DiseaseInfo.builder()
                        .diseaseName(primaryCandidate.getDiseaseKo())
                        .confidenceScore(primaryCandidate.getProbability())
                        .description(primaryCandidate.getDescription())
                        .causes(primaryCandidate.getCauses())
                        .managementTips(primaryCandidate.getCareTips())
                        .build())
                .build();
    }

    // CANDIDATES_2_RETAKE 처리
    private DiagnosisResponseDto handleCandidates2Retake(CustomVisionResponseDto cvResponse, String crop, String imageUrl) {
        List<DiagnosisResponseDto.DiseaseCandidate> candidates = new ArrayList<>();
        
        for (int i = 0; i < Math.min(2, cvResponse.getPredictions().size()); i++) {
            var pred = cvResponse.getPredictions().get(i);
            TagMeta meta = TAG_META_MAP.get(pred.getTagName());
            
            candidates.add(DiagnosisResponseDto.DiseaseCandidate.builder()
                    .diseaseName(meta.diseaseKo)
                    .confidenceScore(pred.getProbability())
                    .rank(i + 1)
                    .build());
        }
        
        return DiagnosisResponseDto.builder()
                .imageUrl(imageUrl)
                .caseType(CaseType.CANDIDATES_2_RETAKE)
                .crop(crop)
                .candidates(candidates)
                .build();
    }

    //  UNDETERMINED_RETAKE 처리
    private DiagnosisResponseDto handleUndeterminedRetake(String crop, String imageUrl) {
        return DiagnosisResponseDto.builder()
                .imageUrl(imageUrl)
                .caseType(CaseType.UNDETERMINED_RETAKE)
                .crop(crop)
                .build();
    }

    // HEALTHY 처리
    private DiagnosisResponseDto handleHealthy(CustomVisionResponseDto cvResponse, String crop, String imageUrl) {
        var top1 = cvResponse.getPredictions().get(0);
        
        // GPT로 관리 팁 생성
        List<String> careTips = openAIClient.generateHealthyCareTips(crop);
        
        return DiagnosisResponseDto.builder()
                .imageUrl(imageUrl)
                .caseType(CaseType.HEALTHY)
                .crop(crop)
                .confidenceScore(top1.getProbability()) // Healthy confidence score
                .careTips(careTips)
                .build();
    }

    // GPT 프롬프트 요청 생성
    private GptPromptRequestDto buildPromptRequest(CaseType caseType, String crop, CustomVisionResponseDto cvResponse) {
        var predictions = cvResponse.getPredictions();
        
        GptPromptRequestDto.GptPromptRequestDtoBuilder builder = GptPromptRequestDto.builder()
                .caseType(caseType)
                .crop(crop);
        
        if (caseType == CaseType.CERTAIN_DISEASE) {
            var top1 = predictions.get(0);
            TagMeta meta = TAG_META_MAP.get(top1.getTagName());
            
            builder.primaryDisease(GptPromptRequestDto.DiseaseCandidate.builder()
                    .tag(top1.getTagName())
                    .crop(meta.crop)
                    .diseaseKo(meta.diseaseKo)
                    .diseaseEn(meta.diseaseEn)
                    .probability(top1.getProbability())
                    .probabilityPercent((int) Math.round(top1.getProbability() * 100))
                    .rank(1)
                    .build());
        } else {
            List<GptPromptRequestDto.DiseaseCandidate> candidates = new ArrayList<>();
            int limit = caseType == CaseType.CANDIDATES_3 ? 3 : 2;
            
            for (int i = 0; i < Math.min(limit, predictions.size()); i++) {
                var pred = predictions.get(i);
                TagMeta meta = TAG_META_MAP.get(pred.getTagName());
                
                candidates.add(GptPromptRequestDto.DiseaseCandidate.builder()
                        .tag(pred.getTagName())
                        .crop(meta.crop)
                        .diseaseKo(meta.diseaseKo)
                        .diseaseEn(meta.diseaseEn)
                        .probability(pred.getProbability())
                        .probabilityPercent((int) Math.round(pred.getProbability() * 100))
                        .rank(i + 1)
                        .build());
            }
            
            builder.candidates(candidates);
            
            if (caseType == CaseType.CANDIDATES_3) {
                builder.primaryDisease(candidates.get(0));
            }
        }
        
        return builder.build();
    }

    // 건강 태그 여부 확인
    private boolean isHealthyTag(String tagName) {
        TagMeta meta = TAG_META_MAP.get(tagName);
        return meta != null && meta.isHealthy;
    }

    // 태그 메타 정보
    private record TagMeta(String crop, String diseaseKo, String diseaseEn, boolean isHealthy) {}

    // 건강 결과 저장 (Response용)
    private void saveHealthyResult(Diagnosis diagnosis, DiagnosisResponseDto response) {
        String preventionMethod = String.join("\n", response.getCareTips());
        
        HealthyResult healthyResult = new HealthyResult(
                diagnosis,
                BigDecimal.valueOf(response.getConfidenceScore()),
                preventionMethod
        );
        
        healthyResultRepository.save(healthyResult);
    }

    // 병충해 결과 저장 (Response용)
    private void saveDiseaseResult(Diagnosis diagnosis, DiagnosisResponseDto response) {
        var diseaseInfo = response.getPrimaryDisease();
        
        String causes = diseaseInfo.getCauses() != null 
                ? String.join("\n", diseaseInfo.getCauses()) 
                : null;
        
        String managementGuide = diseaseInfo.getManagementTips() != null 
                ? String.join("\n", diseaseInfo.getManagementTips()) 
                : null;
        
        DiseaseResult diseaseResult = new DiseaseResult(
                diagnosis,
                diseaseInfo.getDiseaseName(),
                BigDecimal.valueOf(diseaseInfo.getConfidenceScore()),
                diseaseInfo.getDescription(),
                causes,
                managementGuide
        );
        
        diseaseResultRepository.save(diseaseResult);
    }
}
