package com.creditscoring.service;

import com.creditscoring.dto.request.SimulationRequest;
import com.creditscoring.dto.reponse.SimulationResponse;

public interface SimulationService {

    SimulationResponse simulerCredit(SimulationRequest request);

}