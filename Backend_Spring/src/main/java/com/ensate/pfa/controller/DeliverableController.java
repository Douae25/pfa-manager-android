package com.ensate.pfa.controller;

import com.ensate.pfa.dto.request.DeliverableRequest;
import com.ensate.pfa.dto.response.DeliverableResponse;
import com.ensate.pfa.entity.enums.DeliverableType;
import com.ensate.pfa.service.DeliverableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliverables")
@RequiredArgsConstructor
public class DeliverableController {

    private final DeliverableService deliverableService;

    // Student use case: Deposit deliverables before/after defense (Deposer les livrables)
    @PostMapping
    public ResponseEntity<DeliverableResponse> depositDeliverable(@Valid @RequestBody DeliverableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deliverableService.depositDeliverable(request));
    }

    // Student use case: View deliverable by ID (for consult)
    @GetMapping("/{id}")
    public ResponseEntity<DeliverableResponse> getDeliverableById(@PathVariable Long id) {
        return ResponseEntity.ok(deliverableService.getDeliverableById(id));
    }

    // Student use case: View all deliverables for their PFA
    @GetMapping("/pfa/{pfaId}")
    public ResponseEntity<List<DeliverableResponse>> getDeliverablesByPfa(@PathVariable Long pfaId) {
        return ResponseEntity.ok(deliverableService.getDeliverablesByPfa(pfaId));
    }

    // Student use case: View deliverables by type (specialization, final report, etc)
    @GetMapping("/pfa/{pfaId}/type/{type}")
    public ResponseEntity<List<DeliverableResponse>> getDeliverablesByPfaAndType(
            @PathVariable Long pfaId,
            @PathVariable DeliverableType type) {
        return ResponseEntity.ok(deliverableService.getDeliverablesByPfaAndType(pfaId, type));
    }
}
