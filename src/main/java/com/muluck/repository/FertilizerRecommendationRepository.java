package com.muluck.repository;

import com.muluck.domain.FertilizerRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FertilizerRecommendationRepository extends JpaRepository<FertilizerRecommendation, UUID> {
    
    List<FertilizerRecommendation> findAll();
}
