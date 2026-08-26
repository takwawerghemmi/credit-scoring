package com.creditscoring.repository;

import com.creditscoring.entity.CreditScore;
import com.creditscoring.entity.DecisionCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecisionCreditRepository
        extends JpaRepository<DecisionCredit, Long> {

    Optional<DecisionCredit> findByCreditScore(CreditScore creditScore);
}
