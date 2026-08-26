package com.creditscoring.controller;

import com.creditscoring.dto.request.SimulationRequest;
import com.creditscoring.dto.reponse.SimulationResponse;
import com.creditscoring.service.SimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping
    public ResponseEntity<SimulationResponse> simulerCredit(
            @Valid @RequestBody SimulationRequest request) {

        SimulationResponse response = simulationService.simulerCredit(request);

        return ResponseEntity.ok(response);
    }
}