package com.muluck.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "허용되지 않은 메서드입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "U002", "잘못된 사용자 ID 형식입니다."),

    // PlantFolder
    PLANT_FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "식물 폴더를 찾을 수 없습니다."),
    PLANT_FOLDER_DUPLICATE(HttpStatus.BAD_REQUEST, "P002", "이미 존재하는 폴더 이름입니다."),

    // Diagnosis
    DIAGNOSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "진단 기록을 찾을 수 없습니다."),
    INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "D002", "지원하지 않는 이미지 형식입니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "D003", "이미지 업로드에 실패했습니다."),

    // Result
    RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "진단 결과를 찾을 수 없습니다."),

    // Azure
    AZURE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A001", "Azure 서비스 연결에 실패했습니다."),
    AZURE_AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "A002", "AI 분석에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
