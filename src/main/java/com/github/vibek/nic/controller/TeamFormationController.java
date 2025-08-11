// src/main/java/com/github/vibek/nic/controller/TeamFormationController.java
package com.github.vibek.nic.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.vibek.nic.dto.TeamFormationDTO;
import com.github.vibek.nic.dto.TeamDto.ManualTeamFormationRequest;
import com.github.vibek.nic.dto.TeamDto.TeamResponseDTO;
import com.github.vibek.nic.entity.Subdivision;
import com.github.vibek.nic.repository.SubdivisionRepository;
import com.github.vibek.nic.service.TeamFormationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/team-formations")
public class TeamFormationController {

    @Autowired
    private TeamFormationService teamFormationService;

    // =================================================================
    // ADDED: Inject the repository to look up subdivisions by name.
    // =================================================================
    @Autowired
    private SubdivisionRepository subdivisionRepository;

    @GetMapping("/teams")
    public List<TeamFormationDTO> getAllTeams() {
        return teamFormationService.getAllTeams();
    }
    
    @GetMapping("/responses/pending")
    public ResponseEntity<List<TeamResponseDTO>> getPendingResponses() {
        try {
            List<TeamResponseDTO> pendingResponses = teamFormationService.getPendingResponses();
            return ResponseEntity.ok(pendingResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamFormationDTO> getTeamFormationById(@PathVariable UUID id) {
        TeamFormationDTO dto = teamFormationService.getTeamFormationById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<TeamFormationDTO> getTeamFormationByCaseId(@PathVariable UUID caseId) {
        TeamFormationDTO dto = teamFormationService.getTeamFormationByCaseId(caseId);
        return ResponseEntity.ok(dto);
    }

    // =================================================================
    // MODIFIED: This endpoint now looks up the Subdivision entity before calling the service.
    // =================================================================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> createTeamFormation(
            @RequestParam UUID caseId,
            @RequestParam String subdivision) {
        try {
            // Step 1: Find the Subdivision entity using the name provided in the request.
            Subdivision subdivisionEntity = subdivisionRepository.findByName(subdivision)
                    .orElseThrow(() -> new RuntimeException("Subdivision with name '" + subdivision + "' not found."));

            // Step 2: Pass the found Subdivision object to the service method.
            teamFormationService.initiateTeamFormation(caseId, subdivisionEntity);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Team formation initiated successfully for case: " + caseId);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Failed to create team formation: " + e.getMessage());
        }
    }
    
    @PutMapping("/{teamId}/response")
    public ResponseEntity<String> handleTeamResponse(
            @PathVariable UUID teamId,
            @RequestParam UUID personId,
            @RequestParam String department,
            @RequestParam String status) {
        try {
            if (!status.equalsIgnoreCase("ACCEPTED") && !status.equalsIgnoreCase("REJECTED")) {
                return ResponseEntity.badRequest()
                        .body("Invalid status. Must be 'ACCEPTED' or 'REJECTED'");
            }

            teamFormationService.handleResponse(teamId, personId, department, status.toUpperCase());
            return ResponseEntity.ok("Response recorded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Failed to handle response: " + e.getMessage());
        }
    }
    
    @GetMapping("/{teamId}/pending-responses")
    public ResponseEntity<List<TeamResponseDTO>> getPendingResponsesByTeam(@PathVariable UUID teamId) {
        try {
            List<TeamResponseDTO> pendingResponses = teamFormationService.getPendingResponsesByTeam(teamId);
            return ResponseEntity.ok(pendingResponses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamFormationDTO createManualTeam(@Valid @RequestBody ManualTeamFormationRequest req) {
        return teamFormationService.createManualTeam(req);
    }
}