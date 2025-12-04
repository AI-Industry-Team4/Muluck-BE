package com.muluck.repository;

import com.muluck.domain.PlantFolder;
import com.muluck.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlantFolderRepository extends JpaRepository<PlantFolder, UUID> {

    List<PlantFolder> findByUser(User user);

    List<PlantFolder> findByUserAndFolderNameContainingIgnoreCase(User user, String folderName);

    boolean existsByUserAndFolderName(User user, String folderName);
}
