package com.creditscoring.repository;

import com.creditscoring.entity.ResponsableCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResponsableCreditRepository
        extends JpaRepository<ResponsableCredit, Long> {

    List<ResponsableCredit> findByAgenceId(Long agenceId);

    Optional<ResponsableCredit> findByEmail(String email);
}
