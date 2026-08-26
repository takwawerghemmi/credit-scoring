package com.creditscoring.repository;

import com.creditscoring.entity.ResponsableCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResponsableCreditRepository extends JpaRepository<ResponsableCredit, Long> {

    List<ResponsableCredit> findByAgenceId(Long agenceId);
}
