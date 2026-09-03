package com.creditscoring.repository;

import com.creditscoring.entity.Garantie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarantieRepository
        extends JpaRepository<Garantie, Long> {

    List<Garantie> findByDemandeCreditId(Long demandeCreditId);

    List<Garantie> findByDemandeCreditConseillerId(Long conseillerId);

    List<Garantie> findByDemandeCreditResponsableId(Long responsableId);

    List<Garantie> findByDemandeCreditClientId(Long clientId);
}