// src/main/java/com/github/vibek/nic/service/CaseService.java
package com.github.vibek.nic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.vibek.nic.dto.CaseDto.CaseDetailsDTO;
import com.github.vibek.nic.dto.CaseDto.CaseRequestDTO;
import com.github.vibek.nic.dto.CaseDto.CaseResponseDTO;
import com.github.vibek.nic.dto.TeamDto.TeamMemberDTO;
import com.github.vibek.nic.entity.*;
import com.github.vibek.nic.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CaseService {

    @Autowired
    private CaseRepository caseRepository;
    @Autowired
    private CaseDetailsRepository caseDetailsRepository;
    @Autowired
    private TeamFormationService teamFormationService;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private TeamFormationRepository teamFormationRepository;
    
    // ADDED: Repository for looking up Subdivision
    @Autowired
    private SubdivisionRepository subdivisionRepository;

    @Transactional(readOnly = true)
    public Map<String, Long> getCaseStatsByDepartment(String department) {
        List<TeamFormation> teams = teamFormationRepository.findByDepartmentInvolvement(department);
        Map<String, Long> stats = teams.stream()
            .map(TeamFormation::getCaseId)
            .distinct()
            .collect(Collectors.groupingBy(ChildMarriageCase::getStatus, Collectors.counting()));
        
        Map<String, Long> caseStats = new HashMap<>();
        caseStats.put("TOTAL", (long) teams.size());
        caseStats.put("PENDING", stats.getOrDefault("PENDING", 0L));
        caseStats.put("IN_PROGRESS", stats.getOrDefault("IN_PROGRESS", 0L));
        caseStats.put("RESOLVED", stats.getOrDefault("RESOLVED", 0L));
        
        return caseStats;
    }

    @Transactional
    public CaseResponseDTO submitCase(CaseRequestDTO caseRequestDTO) {
        ChildMarriageCase caseEntity = new ChildMarriageCase();
        caseEntity.setComplainantPhone(caseRequestDTO.getComplainantPhone());
        caseEntity.setReportedAt(caseRequestDTO.getReportedAt() != null ? caseRequestDTO.getReportedAt() : LocalDateTime.now());
        caseEntity.setStatus("PENDING");
        ChildMarriageCase savedCase = caseRepository.save(caseEntity);

        Subdivision girlSubdivisionEntity = null;
        if (caseRequestDTO.getCaseDetails() != null) {
            CaseDetails caseDetail = new CaseDetails();
            caseDetail.setCaseId(savedCase);
            caseDetail.setMarriageDate(caseRequestDTO.getCaseDetails().getMarriageDate());
            caseDetail.setBoyName(caseRequestDTO.getCaseDetails().getBoyName());
            caseDetail.setBoyFatherName(caseRequestDTO.getCaseDetails().getBoyFatherName());
            caseDetail.setGirlName(caseRequestDTO.getCaseDetails().getGirlName());
            caseDetail.setGirlFatherName(caseRequestDTO.getCaseDetails().getGirlFatherName());
            caseDetail.setGirlAddress(caseRequestDTO.getCaseDetails().getGirlAddress());
            
            // MODIFIED: Look up and set Subdivision entities
            if (caseRequestDTO.getCaseDetails().getBoySubdivision() != null && !caseRequestDTO.getCaseDetails().getBoySubdivision().isEmpty()) {
                Subdivision boySubdivision = subdivisionRepository.findByName(caseRequestDTO.getCaseDetails().getBoySubdivision())
                        .orElseThrow(() -> new RuntimeException("Boy's subdivision not found: " + caseRequestDTO.getCaseDetails().getBoySubdivision()));
                caseDetail.setBoySubdivision(boySubdivision);
            }
            if (caseRequestDTO.getCaseDetails().getGirlSubdivision() != null && !caseRequestDTO.getCaseDetails().getGirlSubdivision().isEmpty()) {
                girlSubdivisionEntity = subdivisionRepository.findByName(caseRequestDTO.getCaseDetails().getGirlSubdivision())
                        .orElseThrow(() -> new RuntimeException("Girl's subdivision not found: " + caseRequestDTO.getCaseDetails().getGirlSubdivision()));
                caseDetail.setGirlSubdivision(girlSubdivisionEntity);
            }
            
            caseDetail.setMarriageAddress(caseRequestDTO.getCaseDetails().getMarriageAddress());
            caseDetail.setPoliceStationNearMarriageLocation(caseRequestDTO.getCaseDetails().getPoliceStationNearMarriageLocation());
            caseDetailsRepository.save(caseDetail);
            savedCase.getCaseDetails().add(caseDetail);
        }

        if (girlSubdivisionEntity == null) {
            // Find a default subdivision if none is provided. Ensure this default exists in your database.
            girlSubdivisionEntity = subdivisionRepository.findByName("Sadar")
                    .orElseThrow(() -> new RuntimeException("Default subdivision 'Agartala' not found. Please populate geography data."));
        }
        
        teamFormationService.initiateTeamFormation(savedCase.getId(), girlSubdivisionEntity);
        
        ChildMarriageCase finalCaseState = caseRepository.findByIdWithDetails(savedCase.getId()).orElse(savedCase);
        return mapToResponseDTO(finalCaseState);
    }
    
    @Transactional(readOnly = true)
    public CaseResponseDTO getCaseById(UUID id) {
        ChildMarriageCase caseEntity = caseRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        return mapToResponseDTO(caseEntity);
    }

    @Transactional(readOnly = true)
    public List<CaseResponseDTO> getAllCases() {
        return caseRepository.findAllWithDetails().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CaseResponseDTO updateCase(UUID id, CaseRequestDTO caseRequestDTO) {
        ChildMarriageCase caseEntity = caseRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Case not found with ID: " + id));
        caseEntity.setComplainantPhone(caseRequestDTO.getComplainantPhone());
        caseEntity.setReportedAt(caseRequestDTO.getReportedAt() != null ? caseRequestDTO.getReportedAt() : caseEntity.getReportedAt());
        caseEntity.setStatus(caseRequestDTO.getStatus());
        if (caseRequestDTO.getCaseDetails() != null) {
            caseEntity.getCaseDetails().clear(); 
            CaseDetails caseDetail = new CaseDetails();
            // This section needs to be fully implemented if you intend to update case details.
            // For now, it creates a new empty detail.
            caseDetail.setCaseId(caseEntity);
            caseEntity.getCaseDetails().add(caseDetail);
        }
        ChildMarriageCase updatedCase = caseRepository.save(caseEntity);
        return mapToResponseDTO(updatedCase);
    }

    @Transactional
    public void deleteCase(UUID id) {
        caseRepository.deleteById(id);
    }

    public CaseResponseDTO mapToResponseDTO(ChildMarriageCase caseEntity) {
        CaseResponseDTO dto = new CaseResponseDTO();
        dto.setId(caseEntity.getId());
        dto.setComplainantPhone(caseEntity.getComplainantPhone());
        dto.setReportedAt(caseEntity.getReportedAt());
        dto.setStatus(caseEntity.getStatus());
        dto.setCreatedAt(caseEntity.getCreatedAt());
        dto.setUpdatedAt(caseEntity.getUpdatedAt());
        dto.setCaseDetails(caseEntity.getCaseDetails().stream()
                .map(detail -> mapToCaseDetailsDTO(detail, caseEntity))
                .collect(Collectors.toList()));
        return dto;
    }

    private CaseDetailsDTO mapToCaseDetailsDTO(CaseDetails caseDetails, ChildMarriageCase caseEntity) {
        CaseDetailsDTO dto = new CaseDetailsDTO();
        dto.setId(caseDetails.getId());
        dto.setCaseId(caseDetails.getCaseId().getId());
        dto.setCreatedAt(caseDetails.getCreatedAt());
        dto.setUpdatedAt(caseDetails.getUpdatedAt());
        dto.setMarriageDate(caseDetails.getMarriageDate());
        dto.setBoyName(caseDetails.getBoyName());
        dto.setBoyFatherName(caseDetails.getBoyFatherName());
        dto.setBoyAddress(caseDetails.getBoyAddress());
        
        // MODIFIED: Map entity objects back to String names for the DTO
        if (caseDetails.getBoySubdivision() != null) {
            dto.setBoySubdivision(caseDetails.getBoySubdivision().getName());
        }
        if (caseDetails.getGirlSubdivision() != null) {
            dto.setGirlSubdivision(caseDetails.getGirlSubdivision().getName());
        }

        dto.setGirlName(caseDetails.getGirlName());
        dto.setGirlFatherName(caseDetails.getGirlFatherName());
        dto.setGirlAddress(caseDetails.getGirlAddress());
        dto.setTeamId(caseDetails.getTeamId());
        dto.setMarriageAddress(caseDetails.getMarriageAddress());
        dto.setMarriageLandmark(caseDetails.getMarriagelocationlandmark());
        dto.setPoliceStationNearMarriageLocation(caseDetails.getPoliceStationNearMarriageLocation());

        TeamFormation team = caseEntity.getTeamFormation();
        if (team != null) {
            List<UUID> allMemberIds = new ArrayList<>();
            if (team.getSupervisor() != null) {
                allMemberIds.add(team.getSupervisor().getId());
            }
            team.getMembers().forEach(member -> allMemberIds.add(member.getId()));

            List<Person> teamPersons = personRepository.findAllById(allMemberIds);
            List<TeamMemberDTO> teamMemberDetails = teamPersons.stream()
                    .map(this::mapPersonToTeamMemberDTO)
                    .collect(Collectors.toList());
            dto.setTeamMembers(teamMemberDetails);
        } else {
            dto.setTeamMembers(Collections.emptyList());
        }
        return dto;
    }

    private TeamMemberDTO mapPersonToTeamMemberDTO(Person person) {
        return new TeamMemberDTO(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getRole() != null ? person.getRole().toString() : "N/A",
                person.getDepartment(),
                person.getDesignation(),
                person.getPhoneNumber()
        );
    }
}