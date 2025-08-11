package com.github.vibek.nic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.vibek.nic.dto.CaseDto.CaseRequestDTO;
import com.github.vibek.nic.dto.CaseDto.CaseResponseDTO;
import com.github.vibek.nic.service.CaseService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    @Autowired
    private CaseService caseService;

    // ====================== NEW ENDPOINT START ======================
    /**
     * Gets case statistics (total, active, resolved, etc.) for a given department.
     * This is used by supervisory dashboards (OC, SDM, DM).
     * @param departmentName The name of the department (e.g., "POLICE").
     * @return A map of case counts by status.
     */
    @GetMapping("/department/{departmentName}/stats")
    public ResponseEntity<Map<String, Long>> getCaseStatsByDepartment(@PathVariable String departmentName) {
        Map<String, Long> stats = caseService.getCaseStatsByDepartment(departmentName);
        return ResponseEntity.ok(stats);
    }
    // ======================= NEW ENDPOINT END =======================

    @PostMapping
    public ResponseEntity<CaseResponseDTO> submitCase(@RequestBody CaseRequestDTO caseRequestDTO) {
        CaseResponseDTO response = caseService.submitCase(caseRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseResponseDTO> getCaseById(@PathVariable UUID id) {
        CaseResponseDTO response = caseService.getCaseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CaseResponseDTO>> getAllCases() {
        List<CaseResponseDTO> cases = caseService.getAllCases();
        return ResponseEntity.ok(cases);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CaseResponseDTO> updateCase(@PathVariable UUID id, @RequestBody CaseRequestDTO caseRequestDTO) {
        CaseResponseDTO response = caseService.updateCase(id, caseRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<String> getCaseStatus(@PathVariable UUID id) {
        CaseResponseDTO response = caseService.getCaseById(id);
        return ResponseEntity.ok(response.getStatus());
    }
}