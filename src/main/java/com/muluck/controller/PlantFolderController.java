package com.muluck.controller;

import com.muluck.dto.CreateFolderResponse;
import com.muluck.dto.PlantFolderListResponse;
import com.muluck.global.response.ApiResponse;
import com.muluck.service.PlantFolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Home", description = "홈화면 폴더 관련 API")
@RestController
@RequestMapping("/api/v1/home/folders")
@RequiredArgsConstructor
public class PlantFolderController {

    private final PlantFolderService plantFolderService;

    @Operation(summary = "홈화면 사용자 폴더 목록 조회", description = "사용자 폴더 목록을 조회." + "`sortBy` 파라미터를 사용해 `latest`(최신순) 또는 `name`(이름순)으로 정렬")
    @GetMapping
    public ResponseEntity<ApiResponse<PlantFolderListResponse>> getFolders(
            @RequestHeader("User-Id") UUID userId,
            @RequestParam(required = false, defaultValue = "latest") String sortBy
    ) {
        PlantFolderListResponse response = plantFolderService.getFolders(userId, sortBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "홈화면 사용자 폴더 목록에서 검색", description = "사용자의 폴더 중 이름이 검색어와 일치하거나 포함되는 폴더를 조회")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PlantFolderListResponse>> searchFolders(
            @RequestHeader("User-Id") UUID userId,
            @RequestParam String keyword
    ) {
        PlantFolderListResponse response = plantFolderService.searchFolders(userId, keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "새로운 폴더 생성",
            description = "사용자가 새 폴더를 생성. `folderName` 파라미터로 폴더 이름 지정 가능 (중복되는 이름 불가)")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateFolderResponse>> createFolder(
            @RequestHeader("User-Id") UUID userId,
            @RequestParam String folderName
    ) {
        CreateFolderResponse response = plantFolderService.createFolder(userId, folderName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
