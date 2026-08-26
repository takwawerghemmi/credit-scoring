package com.creditscoring.dto.reponse;

import lombok.Data;

import java.util.List;

@Data
public class DashboardExecutifResponse {

    private KpiAvanceResponse kpi;

    private List<ComparaisonMoisResponse> comparaisonMois;

    private List<ComparaisonAnneeResponse> comparaisonAnnee;

    private List<ClassementClientResponse> classementClients;

    private List<ClassementConseillerResponse> classementConseillers;

    private List<ClassementBanqueResponse> classementBanques;

    private List<ClassementAgenceResponse> classementAgences;

    private List<Object[]> topClients;

    private List<Object[]> topAgences;

    private List<Object[]> topConseillers;

    private List<Object[]> fraudes;

}