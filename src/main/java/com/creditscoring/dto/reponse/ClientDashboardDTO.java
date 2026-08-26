package com.creditscoring.dto.reponse;

import com.creditscoring.entity.DemandeCredit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardDTO {

    private Long clientId;
    private String nom;
    private String prenom;
    private String email;

    private Double creditScore;
    private String niveauRisque;

    private Long totalApplications;
    private Long pendingApplications;
    private Long approvedApplications;
    private Long rejectedApplications;

    private Double totalRequestedAmount;
    private Double totalApprovedAmount;

    private Long unreadNotifications;

    private List<DemandeCredit> recentApplications;

    private Map<String, Long> applicationStatusDistribution;
}
