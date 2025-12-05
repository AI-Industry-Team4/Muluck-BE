package com.muluck.controller;

import com.muluck.dto.FertilizerProductDto;
import com.muluck.global.response.ApiResponse;
import com.muluck.service.FertilizerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fertilizers")
@RequiredArgsConstructor
@Tag(name = "Fertilizer", description = "비료 추천 API")
public class FertilizerController {

    private final FertilizerService fertilizerService;

    @GetMapping("/products")
    @Operation(summary = "추천 비료 제품 조회", description = "추천 비료 제품 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FertilizerProductDto>>> getRecommendedProducts() {
        List<FertilizerProductDto> products = fertilizerService.getRecommendedProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
