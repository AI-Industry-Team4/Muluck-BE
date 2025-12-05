package com.muluck.dto.diagnosis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisRequestDto {
    private MultipartFile image;
    private Long folderId;
    private String imageSource; // "CAMERA" 또는 "GALLERY"
}
