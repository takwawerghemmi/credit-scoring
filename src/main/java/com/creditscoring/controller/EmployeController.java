package com.creditscoring.controller;

import com.creditscoring.dto.request.CreateEmployeRequest;
import com.creditscoring.dto.reponse.UtilisateurResponse;
import com.creditscoring.service.EmployeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EmployeController {

    private final EmployeService employeService;
    @PostMapping("/admin")
    public ResponseEntity<UtilisateurResponse> creerAdmin(
            @Valid @RequestBody CreateEmployeRequest request){

        return ResponseEntity.ok(
                employeService.creerAdministrateur(request));
    }
    @PostMapping("/conseiller")
    public ResponseEntity<UtilisateurResponse> creerConseiller(
            @Valid @RequestBody CreateEmployeRequest request){

        return ResponseEntity.ok(
                employeService.creerConseiller(request));
    }
    @PostMapping("/directeur")
    public ResponseEntity<UtilisateurResponse> creerDirecteur(
            @Valid @RequestBody CreateEmployeRequest request){

        return ResponseEntity.ok(
                employeService.creerDirecteur(request));
    }
    @PostMapping("/responsable-credit")
    public ResponseEntity<UtilisateurResponse> creerResponsableCredit(
            @Valid @RequestBody CreateEmployeRequest request){

        return ResponseEntity.ok(
                employeService.creerResponsableCredit(request));
    }

}