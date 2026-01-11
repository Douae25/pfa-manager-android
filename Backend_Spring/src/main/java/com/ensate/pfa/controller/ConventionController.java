package com.ensate.pfa.controller;

import com.ensate.pfa.dto.request.ConventionRequest;
import com.ensate.pfa.dto.response.ConventionResponse;
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
}
