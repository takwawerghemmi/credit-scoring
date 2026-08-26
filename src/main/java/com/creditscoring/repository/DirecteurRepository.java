package com.creditscoring.repository;

import com.creditscoring.entity.Directeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirecteurRepository extends JpaRepository<Directeur, Long> {

    List<Directeur> findByBanqueId(Long banqueId);
}
