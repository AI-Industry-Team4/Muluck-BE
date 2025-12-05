package com.muluck.service;

import com.muluck.domain.Diagnosis;
import com.muluck.domain.PlantFolder;
import com.muluck.domain.User;
import com.muluck.dto.DiagnosisItemListResponse;
import com.muluck.dto.DiagnosisItemResponse;
import com.muluck.global.exception.BaseException;
import com.muluck.global.exception.ErrorCode;
import com.muluck.repository.DiagnosisRepository;
import com.muluck.repository.PlantFolderRepository;
import com.muluck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderDiagnosisService {

    private final PlantFolderRepository plantFolderRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;

    /**
     * 폴더에서 해당 작물의 진단기록 리스트 조회
     */
    public DiagnosisItemListResponse getFolderDiagnoses(UUID userId, UUID folderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        PlantFolder folder = plantFolderRepository.findById(folderId)
                .filter(f -> f.getUser().equals(user))
                .orElseThrow(() -> new BaseException(ErrorCode.PLANT_FOLDER_NOT_FOUND));

        List<Diagnosis> diagnoses = diagnosisRepository.findByPlantFolderInWithResults(List.of(folder));

        List<DiagnosisItemResponse> diagnosisResponses = diagnoses.stream()
                .map(DiagnosisItemResponse::of)
                .sorted(Comparator.comparing(DiagnosisItemResponse::diagnosisDate).reversed())
                .collect(Collectors.toList());

        return DiagnosisItemListResponse.of(folder.getFolderId(), folder.getFolderName(), diagnosisResponses);
    }

    /**
     * 폴더에서 진단명으로 작물 검색
     */
    public DiagnosisItemListResponse searchFolderDiagnoses(UUID userId, UUID folderId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        PlantFolder folder = plantFolderRepository.findById(folderId)
                .filter(f -> f.getUser().equals(user))
                .orElseThrow(() -> new BaseException(ErrorCode.PLANT_FOLDER_NOT_FOUND));

        List<Diagnosis> diagnoses = diagnosisRepository.findByPlantFolderInWithResults(List.of(folder));

        List<DiagnosisItemResponse> filtered = diagnoses.stream()
                .filter(d -> "DISEASE".equalsIgnoreCase(d.getDiagnosisType())
                        && d.getDiseaseResult() != null
                        && d.getDiseaseResult().getDiseaseName().toLowerCase().contains(keyword.toLowerCase()))
                .map(DiagnosisItemResponse::of)
                .sorted(Comparator.comparing(DiagnosisItemResponse::diagnosisDate).reversed())
                .collect(Collectors.toList());

        return DiagnosisItemListResponse.of(folder.getFolderId(), folder.getFolderName(), filtered);
    }

    /**
     * 진단 기록을 다른 폴더로 이동
     */
    @Transactional
    public void moveDiagnosis(UUID userId, UUID diagnosisId, UUID targetFolderId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new BaseException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        if (!diagnosis.getPlantFolder().getUser().getUserId().equals(userId)) {
            throw new BaseException(ErrorCode.USER_NOT_AUTHORIZED);
        }

        PlantFolder targetFolder = plantFolderRepository.findById(targetFolderId)
                .orElseThrow(() -> new BaseException(ErrorCode.PLANT_FOLDER_NOT_FOUND));

        if (!targetFolder.getUser().getUserId().equals(userId)) {
            throw new BaseException(ErrorCode.USER_NOT_AUTHORIZED);
        }

        diagnosis.moveToFolder(targetFolder);
    }
}