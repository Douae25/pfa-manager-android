package com.ensate.pfa.controller;

import com.ensate.pfa.dto.request.PFADossierRequest;
import com.ensate.pfa.dto.response.PFADossierResponse;
import com.ensate.pfa.service.PFADossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/pfa-dossiers")
@RequiredArgsConstructor
public class PFADossierController {

    private final PFADossierService pfaDossierService;

    // Student use case: Create or get existing PFA dossier
    @PostMapping("/create-or-get")
    public ResponseEntity<PFADossierResponse> createOrGetDossier(@Valid @RequestBody PFADossierRequest request) {
        PFADossierResponse response = pfaDossierService.createOrGetDossier(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Student use case: Get PFA dossier by ID
    @GetMapping("/{id}")
    public ResponseEntity<PFADossierResponse> getDossierById(@PathVariable Long id) {
        PFADossierResponse response = pfaDossierService.getDossierById(id);
        return ResponseEntity.ok(response);
    }

    // Student use case: Get all PFA dossiers for a student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PFADossierResponse>> getDossiersByStudent(@PathVariable Long studentId) {
        List<PFADossierResponse> responses = pfaDossierService.getDossiersByStudent(studentId);
        return ResponseEntity.ok(responses);
    }
}
