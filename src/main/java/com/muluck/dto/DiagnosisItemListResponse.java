package com.muluck.dto;

import java.util.List;
import java.util.UUID;

public record DiagnosisItemListResponse(
        UUID folderId,
        String folderName,
        List<DiagnosisItemResponse> diagnoses
) {
    public static DiagnosisItemListResponse of(UUID folderId, String folderName, List<DiagnosisItemResponse> diagnoses) {
        return new DiagnosisItemListResponse(folderId, folderName, diagnoses);
    }
}
