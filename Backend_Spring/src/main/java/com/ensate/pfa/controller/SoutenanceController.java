package com.ensate.pfa.controller;

import com.ensate.pfa.dto.response.SoutenanceResponse;
import com.ensate.pfa.service.SoutenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class SoutenanceController {

    private final SoutenanceService soutenanceService;

    // Student use case: Consult defense date (Consulter date de soutenance)
    @GetMapping("/pfa/{pfaId}")
    public ResponseEntity<SoutenanceResponse> getSoutenanceByPfaId(@PathVariable Long pfaId) {
        return ResponseEntity.ok(soutenanceService.getSoutenanceByPfaId(pfaId));
    }
}
