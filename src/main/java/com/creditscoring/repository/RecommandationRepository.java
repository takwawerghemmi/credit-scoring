package com.creditscoring.repository;

import com.creditscoring.entity.Recommandation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommandationRepository extends JpaRepository<Recommandation, Long> {
}