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

    @Query("SELECT DISTINCT d FROM Diagnosis d " +
            "LEFT JOIN FETCH d.diseaseResult " +
            "LEFT JOIN FETCH d.healthyResult " +
            "WHERE d.plantFolder IN :folders")
    List<Diagnosis> findByPlantFolderInWithResults(@Param("folders") List<PlantFolder> folders);
}
