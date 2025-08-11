package com.github.vibek.nic.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.vibek.nic.dto.FeedbackDTO.FeedbackRequestDTO;
import com.github.vibek.nic.dto.FeedbackDTO.FeedbackResponseDTO;
import com.github.vibek.nic.dto.ReportDto.ReportRequestDTO;
import com.github.vibek.nic.dto.ReportDto.ReportResponseDTO;
import com.github.vibek.nic.entity.Report;
import com.github.vibek.nic.entity.ReportFeedback;
import com.github.vibek.nic.service.ReportService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{reportId}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponseDTO giveFeedback(@PathVariable Long reportId, @Valid @RequestBody FeedbackRequestDTO dto) {
        dto.setReportId(reportId);
        ReportFeedback feedback = reportService.giveFeedback(dto);
        return reportService.mapToFeedbackResponse(feedback);
    }

    @GetMapping("/department/{departmentName}")
    public List<ReportResponseDTO> getReportsByDepartment(@PathVariable String departmentName) {
        return reportService.getReportsByDepartment(departmentName).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/{reportId}/approve")
    @ResponseStatus(HttpStatus.OK)
    public void approveReport(@PathVariable Long reportId, @RequestParam UUID supervisorId) {
        reportService.approveReport(reportId, supervisorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponseDTO createReport(@Valid @RequestBody ReportRequestDTO dto) {
        Report report = reportService.submitDepartmentReport(dto.getCaseId(), dto.getPersonId(), dto.getReport(), dto.getDepartment());
        return mapToResponse(report);
    }

    @GetMapping("/{id}")
    public ReportResponseDTO getReport(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return mapToResponse(report);
    }

    @GetMapping("/team-member/{personId}")
    public List<ReportResponseDTO> getReportsByPerson(@PathVariable UUID personId) {
        return reportService.getReportsByPersonId(personId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ReportResponseDTO updateReport(@PathVariable Long id, @RequestParam UUID personId, @RequestBody String newContent) {
        Report report = reportService.updateReport(id, personId, newContent);
        return mapToResponse(report);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long id, @RequestParam UUID supervisorId) {
        reportService.deleteReport(id, supervisorId);
    }

    @PostMapping("/merge")
    public ReportResponseDTO mergeReports(@RequestParam UUID caseId, @RequestParam UUID supervisorId) {
        Report finalReport = reportService.mergeReports(caseId, supervisorId);
        return mapToResponse(finalReport);
    }

    @GetMapping("/case/{caseId}")
    public List<ReportResponseDTO> getAllReportsByCase(@PathVariable UUID caseId) {
        return reportService.getReportsByCaseId(caseId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @GetMapping("/case/{caseId}/department")
    public List<ReportResponseDTO> getDepartmentReportsByCase(@PathVariable UUID caseId)  {
        return reportService.getDepartmentReportsByCaseId(caseId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @GetMapping("/case/{caseId}/final")
    public ResponseEntity<ReportResponseDTO> getFinalReportByCase(@PathVariable UUID caseId)  {
        Optional<Report> finalReport = reportService.getFinalReportByCaseId(caseId);
        return finalReport.map(report -> ResponseEntity.ok(mapToResponse(report)))
                .orElseThrow(() -> new RuntimeException("No final report found for this case"));
    }
    
    @GetMapping("/feedback/pending")
    public List<FeedbackResponseDTO> getPendingFeedback(@RequestParam UUID personId) {
        return reportService.getPendingFeedbackForPerson(personId);
    }
    
    @GetMapping("/{reportId}/feedback")
    public List<FeedbackResponseDTO> getFeedbackForReport(@PathVariable Long reportId) {
        return reportService.getFeedbackForReport(reportId);
    }
    
    @GetMapping("/filter")
    public List<ReportResponseDTO> searchWithFilters(
            @RequestParam(required = false) String boySubdivision,
            @RequestParam(required = false) String girlSubdivision,
            @RequestParam(required = false) String marriageAddress,
            @RequestParam(required = false) String policeStation,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status, // ADDED PARAMETER
            @RequestParam(required = false) String district // ADDED PARAMETER
    ) {
        return reportService.searchWithFilters(
            boySubdivision, girlSubdivision, marriageAddress, policeStation, year, month, department, status, district // PASS PARAMETER
        ).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private ReportResponseDTO mapToResponse(Report report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        if (report.getCaseEntity() != null) {
            dto.setCaseId(report.getCaseEntity().getId());
        }
        dto.setPersonId(report.getPersonId());
        dto.setReport(report.getReport());
        dto.setDepartment(report.getDepartment());
        dto.setSubmittedAt(report.getSubmittedAt());
        dto.setIsFinalReport(report.getIsFinalReport());
        dto.setStatus(report.getStatus());
        return dto;
    }
}