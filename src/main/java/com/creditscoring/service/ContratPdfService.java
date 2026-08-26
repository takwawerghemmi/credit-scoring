package com.creditscoring.service;

import com.creditscoring.entity.Contrat;

public interface ContratPdfService {

    byte[] genererContratPdf(Contrat contrat);
}