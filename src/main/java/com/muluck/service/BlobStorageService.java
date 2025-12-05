package com.muluck.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlobStorageService {

    private final BlobContainerClient blobContainerClient;

    // 이미지를 Blob Storage에 업로드하고 URL을 반환
    public String uploadImage(MultipartFile file) {
        try {
            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            
            String blobName = UUID.randomUUID() + extension;
            
            // Blob 클라이언트 생성
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            
            // Content-Type 설정
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());
            
            // 업로드
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);
            
            // URL 반환
            String imageUrl = blobClient.getBlobUrl();
            log.info("Image uploaded successfully: {}", imageUrl);
            
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload image to blob storage", e);
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }
    }

    // 에러 발생 시 Blob Storage에서 이미지 삭제
    public void deleteImage(String imageUrl) {
        try {
            // URL에서 blob 이름 추출
            String blobName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            
            if (blobClient.exists()) {
                blobClient.delete();
                log.info("Image deleted successfully: {}", imageUrl);
            }
        } catch (Exception e) {
            log.error("Failed to delete image from blob storage: {}", imageUrl, e);
        }
    }
}
