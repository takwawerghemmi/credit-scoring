package com.creditscoring.repository;

import com.creditscoring.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    boolean existsByNomAndDemandeCreditId(
            String nom,
            Long demandeCreditId
    );

}