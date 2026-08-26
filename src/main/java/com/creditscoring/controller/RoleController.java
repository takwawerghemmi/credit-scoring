package com.creditscoring.controller;

import com.creditscoring.entity.Role;
import com.creditscoring.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RoleController {

    private final RoleService roleService;



    @GetMapping
    public ResponseEntity<List<Role>> afficherTous() {

        return ResponseEntity.ok(roleService.afficherTousLesRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(@PathVariable Long id) {

        return roleService.trouverRoleParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }




}