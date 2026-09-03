package com.creditscoring.repository;

import com.creditscoring.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    boolean existsByNomAndDemandeCreditId(
            String nom,
            Long demandeCreditId
    );

    List<Document> findByDemandeCreditId(Long demandeCreditId);
}