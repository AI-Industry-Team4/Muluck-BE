package com.muluck.dto;

import java.util.List;
import java.util.UUID;

public record PlantFolderResponse(
        UUID folderId,
        String folderName,
        List<String> recentImageUrls
) {
}
