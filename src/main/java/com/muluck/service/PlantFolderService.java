package com.muluck.service;

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
        User user = findUserByStringId(userId);

        List<PlantFolder> folders = plantFolderRepository.findByUser(user);

        if ("name".equalsIgnoreCase(sortBy)) {
            folders.sort(Comparator.comparing(PlantFolder::getFolderName));
        } else {
            folders.sort(Comparator.comparing(PlantFolder::getCreatedAt).reversed());
        }

        List<PlantFolderResponse> folderResponses = folders.stream().map(folder -> {
            List<String> recentImages = diagnosisRepository.findTop4ImageUrlsByFolder(folder)
                    .stream()
                    .limit(4)
                    .collect(Collectors.toList());
            return new PlantFolderResponse(folder.getFolderId(), folder.getFolderName(), recentImages);
        }).toList();

        return PlantFolderListResponse.of(folderResponses);
    }

    /**
     * 폴더 검색
     */
    public PlantFolderListResponse  searchFolders(UUID userId, String keyword) {
        User user = findUserByStringId(userId);

        List<PlantFolder> folders = plantFolderRepository.findByUserAndFolderNameContainingIgnoreCase(user, keyword);

        List<PlantFolderResponse> folderResponses = folders.stream().map(folder -> {
            List<String> recentImages = diagnosisRepository.findTop4ImageUrlsByFolder(folder)
                    .stream()
                    .limit(4)
                    .collect(Collectors.toList());
            return new PlantFolderResponse(folder.getFolderId(), folder.getFolderName(), recentImages);
        }).toList();

        return PlantFolderListResponse.of(folderResponses);
    }

    /**
     * 폴더 생성
     */
    @Transactional
    public CreateFolderResponse createFolder(UUID  userId, String folderName) {
        User user = findUserByStringId(userId);

        if (plantFolderRepository.existsByUserAndFolderName(user, folderName)) {
            throw new BaseException(PLANT_FOLDER_DUPLICATE);
        }

        PlantFolder folder = new PlantFolder(user, folderName);
        PlantFolder saved = plantFolderRepository.save(folder);

        return new CreateFolderResponse(saved.getFolderName());
    }

    private User findUserByStringId(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new BaseException(USER_NOT_FOUND));
    }
}