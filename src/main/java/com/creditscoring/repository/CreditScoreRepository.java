package com.creditscoring.repository;

import com.creditscoring.entity.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, Long> {

    Optional<CreditScore> findByDemandeCreditId(Long demandeCreditId);

    List<CreditScore> findByDemandeCreditConseillerId(Long conseillerId);

    List<CreditScore> findByDemandeCreditResponsableId(Long responsableId);

    List<CreditScore> findByDemandeCreditClientId(Long clientId);
}