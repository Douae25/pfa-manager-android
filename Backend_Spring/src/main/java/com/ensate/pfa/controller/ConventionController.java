package com.ensate.pfa.controller;

import com.ensate.pfa.dto.request.ConventionRequest;
import com.ensate.pfa.dto.response.ConventionResponse;
import com.ensate.pfa.dto.request.ConventionAdminActionRequest;
import com.ensate.pfa.dto.ConventionDto;
import com.ensate.pfa.entity.enums.ConventionState;
import com.ensate.pfa.service.ConventionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conventions")
@RequiredArgsConstructor
public class ConventionController {

    private final ConventionService conventionService;

    // Student use case: Request internship convention (Demander convention de stage)
    @PostMapping
    public ResponseEntity<ConventionResponse> requestConvention(@Valid @RequestBody ConventionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conventionService.requestConvention(request));
    }

    // Student use case: Upload signed convention (Deposer convention signee)
    @PostMapping("/{id}/upload-signed")
    public ResponseEntity<ConventionResponse> uploadSignedConvention(
            @PathVariable Long id,
            @RequestParam String scannedFileUri) {
        return ResponseEntity.ok(conventionService.uploadSignedConvention(id, scannedFileUri));
    }


    // ADMIN: Lister toutes les conventions avec mapping d'état frontend
    @GetMapping("/frontend")
    public ResponseEntity<?> getAllConventionsFrontend() {
        return ResponseEntity.ok(conventionService.getAllConventionsFrontend());
    }

    // Student use case: Consult convention (Consulter la convention)
    @GetMapping("/{id}")
    public ResponseEntity<ConventionResponse> getConventionById(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.getConventionById(id));
    }

    // Student use case: Get convention by PFA ID (for consult)
    @GetMapping("/pfa/{pfaId}")
    public ResponseEntity<ConventionResponse> getConventionByPfaId(@PathVariable Long pfaId) {
        return ResponseEntity.ok(conventionService.getConventionByPfaId(pfaId));
    }

    // ADMIN: Traiter une convention (accept/refuse)
    @PutMapping("/{id}/admin-action")
    public ResponseEntity<ConventionResponse> adminActionOnConvention(
            @PathVariable Long id,
            @RequestBody ConventionAdminActionRequest request) {
        return ResponseEntity.ok(conventionService.adminActionOnConvention(id, request));
    }

    // Get all conventions (for sync)
    @GetMapping
    public ResponseEntity<?> getAllConventions() {
        return ResponseEntity.ok(conventionService.getAllConventions());
    }

    // Update convention (for sync)
    @PutMapping("/{id}")
    public ResponseEntity<ConventionResponse> updateConvention(@PathVariable Long id, @Valid @RequestBody ConventionRequest request) {
        return ResponseEntity.ok(conventionService.updateConvention(id, request));
    }
}
