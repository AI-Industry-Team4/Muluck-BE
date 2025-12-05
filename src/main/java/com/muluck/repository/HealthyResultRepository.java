package com.muluck.repository;

import com.muluck.domain.HealthyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthyResultRepository extends JpaRepository<HealthyResult, UUID> {

}
