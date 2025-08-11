package com.github.vibek.nic.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.vibek.nic.dto.WithdrawalDto.*;
import com.github.vibek.nic.service.WithdrawalRequestService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/withdrawal-requests")
@RequiredArgsConstructor
public class WithdrawalRequestController {

    private final WithdrawalRequestService withdrawalRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createRequest(@Valid @RequestBody WithdrawalRequestCreateDTO dto) {
        withdrawalRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Withdrawal request submitted successfully.");
    }

    @GetMapping("/pending")
    public ResponseEntity<List<WithdrawalRequestResponseDTO>> getPendingRequests(
        @RequestParam String department,
        @RequestParam String subdivision) {
        List<WithdrawalRequestResponseDTO> requests = withdrawalRequestService.getPendingRequestsForSupervisor(department, subdivision);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/status")
    public ResponseEntity<WithdrawalRequestResponseDTO> getRequestStatus(
        @RequestParam UUID caseId,
        @RequestParam UUID requesterId) {
        
        return withdrawalRequestService.findByCaseAndRequester(caseId, requesterId)
            .map(request -> ResponseEntity.ok(withdrawalRequestService.mapToResponseDTO(request)))
            .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/approve-reassign")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> approveAndReassign(@PathVariable UUID id, @Valid @RequestBody WithdrawalReassignDTO dto) {
        withdrawalRequestService.approveAndReassign(id, dto);
        return ResponseEntity.ok("Request approved and team member reassigned successfully.");
    }

    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> rejectRequest(@PathVariable UUID id, @RequestParam UUID supervisorId) {
        withdrawalRequestService.rejectRequest(id, supervisorId);
        return ResponseEntity.ok("Request rejected successfully.");
    }
}