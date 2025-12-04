package com.muluck.repository;

import com.muluck.domain.Diagnosis;
import com.muluck.domain.PlantFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID> {

    @Query("SELECT d.imageUrl FROM Diagnosis d " +
            "WHERE d.plantFolder = :folder " +
            "ORDER BY d.diagnosisDate DESC")
    List<String> findTop4ImageUrlsByFolder(@Param("folder") PlantFolder folder);
}
