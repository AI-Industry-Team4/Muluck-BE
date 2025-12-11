package com.muluck.controller;

import com.muluck.dto.DiagnosisDetailResponse;
import com.muluck.dto.DiagnosisItemListResponse;
import com.muluck.global.response.ApiResponse;
import com.muluck.service.FolderDiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Diagnosis", description = "폴더 안 진단기록 관련 API")
@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderDiagnosisController {

    private final FolderDiagnosisService folderDiagnosisService;

    /**
     * 한 폴더의 진단 기록 조회
     */
    @Operation(summary = "한 폴더의 진단 기록 목록 조회", description = "한 사용자의 특정 폴더 내 진단 기록 목록을 조회")
    @GetMapping("/{folderId}/diagnoses")
    public ResponseEntity<ApiResponse<DiagnosisItemListResponse>> getFolderDiagnoses(
            @RequestHeader("User-Id") UUID userId,
            @PathVariable UUID folderId
    ) {
        DiagnosisItemListResponse response = folderDiagnosisService.getFolderDiagnoses(userId, folderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 한 폴더에서 진단기록 검색
     */
    @Operation(summary = "한 폴더에서 진단 기록 검색", description = "사용자의 특정 폴더 내 진단 기록을 `질병명`으로 검색하여 기록 (목록) 조회")
    @GetMapping("/{folderId}/diagnoses/search")
    public ResponseEntity<ApiResponse<DiagnosisItemListResponse>> searchFolderDiagnoses(
            @RequestHeader("User-Id") UUID userId,
            @PathVariable UUID folderId,
            @RequestParam String keyword
    ) {
        DiagnosisItemListResponse response = folderDiagnosisService.searchFolderDiagnoses(userId, folderId, keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 진단 기록을 다른 폴더로 이동
     */
    @Operation(summary = "진단 기록 이동", description = "사용자 폴더 내 진단 기록 하나를 다른 폴더로 이동")
    @PostMapping("/diagnoses/{diagnosisId}/move")
    public ResponseEntity<ApiResponse<Void>> moveDiagnosis(
            @RequestHeader("User-Id") UUID userId,
            @PathVariable UUID diagnosisId,
            @RequestParam UUID targetFolderId
    ) {
        folderDiagnosisService.moveDiagnosis(userId, diagnosisId, targetFolderId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 진단 기록 단 건 상세 조회
     */
    @Operation(summary = "진단 기록 단 건 상세 조회", description = "사용자 폴더 내 진단 기록 하나를 상세 조회")
    @GetMapping("/diagnoses/{diagnosisId}")
    public ResponseEntity<ApiResponse<DiagnosisDetailResponse>> getDiagnosisDetail(
            @RequestHeader("User-Id") UUID userId,
            @PathVariable UUID diagnosisId
    ) {
        DiagnosisDetailResponse response = folderDiagnosisService.getDiagnosisDetail(userId, diagnosisId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
