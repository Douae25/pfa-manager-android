package com.ensate.pfa.controller;

import com.ensate.pfa.dto.request.PFADossierRequest;
import com.ensate.pfa.dto.response.ApiResponse;
import com.ensate.pfa.dto.response.PFADossierDTO;
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
@CrossOrigin(origins = "*")
public class PFADossierController {

    private final PFADossierService pfaDossierService;

    /**
     * Create or get existing PFA dossier
     * POST /api/pfa-dossiers/create-or-get
     */
    @PostMapping("/create-or-get")
    public ResponseEntity<PFADossierResponse> createOrGetDossier(@Valid @RequestBody PFADossierRequest request) {
        PFADossierResponse response = pfaDossierService.createOrGetDossier(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Get PFA dossier by ID
     * GET /api/pfa-dossiers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PFADossierResponse> getDossierById(@PathVariable Long id) {
        PFADossierResponse response = pfaDossierService.getDossierById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all PFA dossiers for a student
     * GET /api/pfa-dossiers/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PFADossierResponse>> getDossiersByStudent(@PathVariable Long studentId) {
        List<PFADossierResponse> responses = pfaDossierService.getDossiersByStudent(studentId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all PFA dossiers
     * GET /api/pfa-dossiers/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PFADossierDTO>>> getAllPFADossiers() {
        // Utiliser le service au lieu du repository
        List<PFADossierDTO> pfas = pfaDossierService.getAllPFADossiers();

        return ResponseEntity.ok(
                ApiResponse.<List<PFADossierDTO>>builder()
                        .success(true)
                        .message("Dossiers PFA récupérés")
                        .data(pfas)
                        .build()
        );
    }
}