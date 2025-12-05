package com.muluck.controller;

import com.muluck.domain.ImageSource;
import com.muluck.dto.diagnosis.DiagnosisResponseDto;
import com.muluck.global.response.ApiResponse;
import com.muluck.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/diagnoses")
@RequiredArgsConstructor
@Tag(name = "Diagnosis", description = "식물 병충해 진단 API")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "식물 병충해 진단 결과 반환", description = "이미지를 분석하여 병충해를 진단하여 결과를 반환합니다.")
    public ApiResponse<DiagnosisResponseDto> analyzeDiagnosis(
            @RequestPart("image") MultipartFile image,
            @RequestHeader("User-Id") UUID userId
    ) {
        DiagnosisResponseDto response = diagnosisService.analyze(image, userId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{tempDiagnosisId}")
    @Operation(summary = "진단 결과 저장", description = "진단 결과를 DB에 저장합니다.")
    public ApiResponse<DiagnosisResponseDto> saveDiagnosis(
            @PathVariable String tempDiagnosisId,
            @RequestParam(required = false) UUID folderId,
            @RequestParam @Parameter(description = "이미지 출처 (CAMERA: 카메라 촬영, GALLERY: 갤러리 선택)") ImageSource imageSource,
            @RequestHeader("User-Id") UUID userId
    ) {
        DiagnosisResponseDto response = diagnosisService.saveDiagnosis(tempDiagnosisId, folderId, imageSource, userId);
        return ApiResponse.success(response);
    }
}
