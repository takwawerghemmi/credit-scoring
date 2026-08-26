package com.creditscoring.service;

import com.creditscoring.entity.Contrat;

public interface SignatureService {

    String signerContrat(Contrat contrat);
}