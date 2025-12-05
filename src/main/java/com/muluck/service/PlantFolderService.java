package com.muluck.service;

import com.muluck.domain.Diagnosis;
import com.muluck.domain.PlantFolder;
import com.muluck.domain.User;
import com.muluck.dto.CreateFolderResponse;
import com.muluck.dto.PlantFolderListResponse;
import com.muluck.dto.PlantFolderResponse;
import com.muluck.global.exception.BaseException;
import com.muluck.repository.DiagnosisRepository;
import com.muluck.repository.PlantFolderRepository;
import com.muluck.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.muluck.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantFolderService {

    private final PlantFolderRepository plantFolderRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;

    /**
     * 폴더 조회
     */
    public PlantFolderListResponse getFolders(UUID userId, String sortBy) {
        User user = findUserByUUID(userId);

        List<PlantFolder> folders = plantFolderRepository.findByUser(user);

        if (folders.isEmpty()) {
            return PlantFolderListResponse.of(List.of());
        }

        List<Diagnosis> allDiagnoses = diagnosisRepository.findByPlantFolderInWithResults(folders);

        Map<UUID, List<String>> folderImagesMap = allDiagnoses.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getPlantFolder().getFolderId(),
                        Collectors.mapping(Diagnosis::getImageUrl, Collectors.toList())
                ));

        Comparator<PlantFolder> comparator = "name".equalsIgnoreCase(sortBy)
                ? Comparator.comparing(PlantFolder::getFolderName)
                : Comparator.comparing(PlantFolder::getCreatedAt).reversed();

        List<PlantFolderResponse> folderResponses = folders.stream()
                .sorted(comparator)
                .map(folder -> new PlantFolderResponse(
                        folder.getFolderId(),
                        folder.getFolderName(),
                        folderImagesMap.getOrDefault(folder.getFolderId(), List.of())
                                .stream()
                                .limit(4)
                                .toList()
                ))
                .toList();

        return PlantFolderListResponse.of(folderResponses);
    }

    /**
     * 폴더 검색
     */
    public PlantFolderListResponse  searchFolders(UUID userId, String keyword) {
        User user = findUserByUUID(userId);

        List<PlantFolder> folders = plantFolderRepository.findByUserAndFolderNameContainingIgnoreCase(user, keyword);

        if (folders.isEmpty()) {
            return PlantFolderListResponse.of(List.of());
        }

        List<Diagnosis> allDiagnoses = diagnosisRepository.findByPlantFolderInWithResults(folders);

        Map<UUID, List<String>> folderImagesMap = allDiagnoses.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getPlantFolder().getFolderId(),
                        Collectors.mapping(Diagnosis::getImageUrl, Collectors.toList())
                ));

        Comparator<PlantFolder> comparator = Comparator.comparing(PlantFolder::getCreatedAt).reversed();

        List<PlantFolderResponse> folderResponses = folders.stream()
                .sorted(comparator)
                .map(folder -> new PlantFolderResponse(
                        folder.getFolderId(),
                        folder.getFolderName(),
                        folderImagesMap.getOrDefault(folder.getFolderId(), List.of())
                                .stream()
                                .limit(4)
                                .toList()
                ))
                .toList();

        return PlantFolderListResponse.of(folderResponses);
    }

    /**
     * 폴더 생성
     */
    @Transactional
    public CreateFolderResponse createFolder(UUID  userId, String folderName) {
        User user = findUserByUUID(userId);

        if (plantFolderRepository.existsByUserAndFolderName(user, folderName)) {
            throw new BaseException(PLANT_FOLDER_DUPLICATE);
        }

        PlantFolder folder = new PlantFolder(user, folderName);
        PlantFolder saved = plantFolderRepository.save(folder);

        return new CreateFolderResponse(saved.getFolderName());
    }

    private User findUserByUUID(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new BaseException(USER_NOT_FOUND));
    }
}