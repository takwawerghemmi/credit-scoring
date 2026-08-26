package com.creditscoring.repository;

import com.creditscoring.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurId(
            Long utilisateurId
    );

    List<Notification> findByLu(
            boolean lu
    );
}