package com.creditscoring.service;

public interface AuditService {

    void enregistrerAction(
            String utilisateur,
            String action,
            String methode,
            String details
    );

}