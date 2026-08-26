package com.creditscoring.dto.request;

public class PaiementRequest {

    private Long echeanceId;

    private String methodePaiement;

    public PaiementRequest() {
    }

    public Long getEcheanceId() {
        return echeanceId;
    }

    public void setEcheanceId(Long echeanceId) {
        this.echeanceId = echeanceId;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }
}