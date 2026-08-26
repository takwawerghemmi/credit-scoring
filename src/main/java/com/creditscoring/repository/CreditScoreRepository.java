package com.creditscoring.repository;

import com.creditscoring.entity.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, Long> {
}