package com.muluck.dto;

import java.util.List;

public record PlantFolderListResponse(
        List<PlantFolderResponse> plantFolders
) {
    public static PlantFolderListResponse of(List<PlantFolderResponse> plantFolders) {
        return new PlantFolderListResponse(plantFolders);
    }
}