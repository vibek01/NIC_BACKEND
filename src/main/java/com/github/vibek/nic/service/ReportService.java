package com.github.vibek.nic.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.github.vibek.nic.dto.FeedbackDTO.FeedbackRequestDTO;
import com.github.vibek.nic.dto.FeedbackDTO.FeedbackResponseDTO;
import com.github.vibek.nic.entity.*;
import com.github.vibek.nic.enums.ReportStatus;
import com.github.vibek.nic.enums.Role;
import com.github.vibek.nic.repository.CaseRepository;
import com.github.vibek.nic.repository.PersonRepository;
import com.github.vibek.nic.repository.ReportFeedbackRepository;
import com.github.vibek.nic.repository.ReportRepository;
import com.github.vibek.nic.repository.TeamFormationRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private TeamFormationRepository teamFormationRepository;
    @Autowired
    private ReportFeedbackRepository feedbackRepository;
    @Autowired
    private CaseRepository caseRepository;
    
    // ====================== ADDED FOR PDF GENERATION START ======================
    @Autowired
    private TemplateEngine templateEngine;
    
    /**
     * Generates a PDF byte array for the final report of a specific case.
     * @param caseId The UUID of the case.
     * @return A byte array representing the generated PDF.
     * @throws IOException If an error occurs during PDF creation.
     */
    @Transactional(readOnly = true)
    public byte[] generateFinalReportPdf(UUID caseId) throws IOException {
        Report finalReport = reportRepository.findByCaseEntity_IdAndIsFinalReportTrue(caseId)
                .orElseThrow(() -> new RuntimeException("Final report not found for case: " + caseId));

        ChildMarriageCase caseEntity = finalReport.getCaseEntity();
        CaseDetails caseDetails = caseEntity.getCaseDetails().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Case details not found for case: " + caseId));

        Person teamLeader = personRepository.findById(finalReport.getPersonId())
                .orElse(null); // The team leader might not be in the DB anymore, handle gracefully

        // Prepare the data for the template
        Context context = new Context();
        context.setVariable("caseEntity", caseEntity);
        context.setVariable("caseDetails", caseDetails);
        context.setVariable("finalReport", finalReport);
        context.setVariable("teamLeader", teamLeader);
        context.setVariable("generationDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy HH:mm")));
        
        // The merged report content may have newlines (\n). HTML needs <br> for line breaks.
        String formattedReportContent = finalReport.getReport().replace("\n", "<br />");
        context.setVariable("formattedReportContent", formattedReportContent);


        // Process the HTML template with the data
        String html = templateEngine.process("report-template", context);

        // Generate PDF from the processed HTML
        try (OutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "/"); // Base URI is needed for relative paths, can be "/"
            builder.toStream(os);
            builder.run();
            return ((ByteArrayOutputStream) os).toByteArray();
        }
    }
    // ======================= ADDED FOR PDF GENERATION END =======================


    @Transactional
    public void approveReport(Long reportId, UUID approverId) {
        Person approver = personRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found with ID: " + approverId));
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        
        Person submitter = personRepository.findById(report.getPersonId())
                .orElseThrow(() -> new RuntimeException("Report submitter not found with ID: " + report.getPersonId()));

        boolean isAuthorized = false;
        String reportDepartment = report.getDepartment();

        switch (reportDepartment.toUpperCase()) {
            case "POLICE":
                if ("SDPO".equalsIgnoreCase(approver.getPostName()) &&
                    approver.getSubdivision() != null &&
                    approver.getSubdivision().equals(submitter.getSubdivision())) {
                    isAuthorized = true;
                }
                break;
            case "ADMINISTRATION":
                if ("SDM".equalsIgnoreCase(approver.getPostName()) &&
                    approver.getSubdivision() != null &&
                    approver.getSubdivision().equals(submitter.getSubdivision())) {
                    isAuthorized = true;
                }
                break;
            case "DICE":
                if ("DISTRICT DICE".equalsIgnoreCase(approver.getPostName()) &&
                    approver.getDistrict() != null &&
                    approver.getDistrict().equals(submitter.getDistrict())) {
                    isAuthorized = true;
                }
                break;
            default:
                throw new IllegalStateException("Approval logic for department '" + reportDepartment + "' is not defined.");
        }

        if (!isAuthorized) {
            throw new SecurityException("User " + approver.getEmail() + " is not authorized to approve this report.");
        }

        report.setStatus(ReportStatus.APPROVED);
        reportRepository.save(report);
    }    
    
    @Transactional
    public ReportFeedback giveFeedback(FeedbackRequestDTO dto) {
        personRepository.findById(dto.getSupervisorId())
            .orElseThrow(() -> new RuntimeException("Supervisor not found with ID: " + dto.getSupervisorId()));
        
        Report report = reportRepository.findById(dto.getReportId())
            .orElseThrow(() -> new RuntimeException("Report not found with ID: " + dto.getReportId()));
        
        report.setStatus(ReportStatus.REJECTED);
        reportRepository.save(report);

        ReportFeedback feedback = ReportFeedback.builder()
            .reportId(dto.getReportId())
            .feedbackFrom(dto.getSupervisorId())
            .feedbackTo(report.getPersonId())
            .feedbackMessage(dto.getFeedbackMessage())
            .status("PENDING")
            .build();
        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<Report> getFinalReportsByDistrict(String districtName) {
        return reportRepository.findFinalReportsByDistrictName(districtName);
    }
    

    public List<Report> getReportsByDepartment(String department) {
        return reportRepository.findByDepartmentOrderBySubmittedAtDesc(department);
    }
    
    @Transactional
    public Report submitDepartmentReport(UUID caseId, UUID personId, String content, String department) {
        ChildMarriageCase caseEntity = caseRepository.findById(caseId)
            .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));

        Person person = personRepository.findById(personId)
            .orElseThrow(() -> new RuntimeException("Person not found with ID: " + personId));
        TeamFormation team = teamFormationRepository.findByCaseId_Id(caseId)
            .orElseThrow(() -> new RuntimeException("No team formed for case with ID: " + caseId));

        boolean isTeamMember = team.getMembers().stream().anyMatch(m -> m.getId().equals(personId));
        boolean isSupervisor = team.getSupervisor() != null && team.getSupervisor().getId().equals(personId);

        if (!isTeamMember && !isSupervisor) {
            throw new RuntimeException("Person " + personId + " is not an assigned member of the team for case " + caseId);
        }
        if (!person.getDepartment().equalsIgnoreCase(department)) {
            throw new RuntimeException("Person " + personId + " cannot submit a report for a different department (" + department + ")");
        }

        if (reportRepository.existsByCaseEntity_IdAndPersonIdAndIsFinalReportFalse(caseId, personId)) {
            throw new RuntimeException("You have already submitted a report for this case.");
        }

        Report report = Report.builder()
                .caseEntity(caseEntity)
                .personId(personId)
                .report(content)
                .department(department)
                .isFinalReport(false)
                .status(ReportStatus.PENDING)
                .build();
        return reportRepository.save(report);
    }

    @Transactional
    public Report updateReport(Long reportId, UUID personId, String newContent) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        if (report.getIsFinalReport()) {
            throw new RuntimeException("Final reports cannot be updated");
        }
        Person person = personRepository.findById(personId).orElseThrow(() -> new RuntimeException("Person not found"));
        if (!report.getPersonId().equals(personId)) {
             throw new RuntimeException("Only the original submitter can update this report.");
        }
        report.setReport(newContent);
        report.setSubmittedAt(LocalDateTime.now());
        report.setStatus(ReportStatus.PENDING); 
        markFeedbackAddressed(reportId, personId);
        return reportRepository.save(report);
    }

    public void deleteReport(Long reportId, UUID supervisorId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
        Person supervisor = personRepository.findById(supervisorId).orElseThrow(() -> new RuntimeException("Person not found"));
        
        if (supervisor.getRole() != Role.SUPERVISOR) {
            throw new RuntimeException("Only supervisors can delete reports");
        }
        if (report.getIsFinalReport()) {
            throw new RuntimeException("Final reports cannot be deleted");
        }
        reportRepository.delete(report);
    }

    public Report getReportById(Long reportId) {
        return reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found"));
    }

    public List<Report> getReportsByCaseId(UUID caseId) {
        return reportRepository.findByCaseEntity_Id(caseId);
    }

    public List<Report> getReportsByPersonId(UUID personId) {
        return reportRepository.findByPersonId(personId);
    }

    public List<Report> getDepartmentReportsByCaseId(UUID caseId) {
        return reportRepository.findByCaseEntity_IdAndIsFinalReportFalse(caseId);
    }

    public Optional<Report> getFinalReportByCaseId(UUID caseId) {
        return reportRepository.findByCaseEntity_IdAndIsFinalReportTrue(caseId);
    }
    
    public List<Report> searchWithFilters(String boySubdivision, String girlSubdivision, String marriageAddress, String policeStation, Integer year, Integer month, String department, String status, String district) {
        ReportStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = ReportStatus.valueOf(status.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                return Collections.emptyList();
            }
        }
        return reportRepository.findByMultipleFilters(boySubdivision, girlSubdivision, marriageAddress, policeStation, year, month, department, statusEnum, district);
    }

    @Transactional
    public Report mergeReports(UUID caseId, UUID mergerId) {
        ChildMarriageCase caseEntity = caseRepository.findById(caseId)
            .orElseThrow(() -> new RuntimeException("Case not found with ID: " + caseId));
        
        TeamFormation team = teamFormationRepository.findByCaseId_Id(caseId)
            .orElseThrow(() -> new RuntimeException("Team not found for case: " + caseId));

        Person designatedLeader = team.getSupervisor();
        if (designatedLeader == null || !designatedLeader.getId().equals(mergerId)) {
            throw new SecurityException("User is not the designated team leader for this case and cannot merge reports.");
        }

        if (reportRepository.findByCaseEntity_IdAndIsFinalReportTrue(caseId).isPresent()) {
            throw new IllegalStateException("A final report already exists for this case.");
        }

        List<Report> deptReports = reportRepository.findByCaseEntity_IdAndIsFinalReportFalse(caseId);
        if (deptReports.isEmpty()) {
            throw new IllegalStateException("No department reports available to merge.");
        }

        for (Report report : deptReports) {
            if (report.getStatus() != ReportStatus.APPROVED) {
                throw new IllegalStateException("Cannot merge: Not all department reports have been approved. Report ID " + report.getId() + " is " + report.getStatus());
            }
        }

        String mergedContent = deptReports.stream()
            .map(report -> {
                Person submitter = personRepository.findById(report.getPersonId()).orElse(null);
                String submitterName = submitter != null ? (submitter.getFirstName() + " " + submitter.getLastName()) : "Unknown";
                return String.format("**%s Department Report by %s:**\n%s", report.getDepartment(), submitterName, report.getReport());
            })
            .collect(Collectors.joining("\n\n--- DEPARTMENT SEPARATOR ---\n\n"));

        Report finalReport = Report.builder()
                .caseEntity(caseEntity)
                .personId(mergerId)
                .report(mergedContent)
                .department("FINAL")
                .isFinalReport(true)
                .status(ReportStatus.APPROVED)
                .build();
        return reportRepository.save(finalReport);
    }
    
    public List<FeedbackResponseDTO> getPendingFeedbackForPerson(UUID personId) {
        return feedbackRepository.findByFeedbackToAndStatus(personId, "PENDING").stream()
            .map(this::mapToFeedbackResponse).collect(Collectors.toList());
    }

    public List<FeedbackResponseDTO> getFeedbackForReport(Long reportId) {
        return feedbackRepository.findByReportIdOrderByCreatedAtDesc(reportId).stream()
            .map(this::mapToFeedbackResponse).collect(Collectors.toList());
    }

    private void markFeedbackAddressed(Long reportId, UUID personId) {
        List<ReportFeedback> pendingFeedbacks = feedbackRepository.findByReportIdOrderByCreatedAtDesc(reportId).stream()
            .filter(f -> f.getStatus().equals("PENDING") && f.getFeedbackTo().equals(personId))
            .collect(Collectors.toList());
        
        pendingFeedbacks.forEach(feedback -> {
            feedback.setStatus("ADDRESSED");
            feedback.setAddressedAt(LocalDateTime.now());
        });
        feedbackRepository.saveAll(pendingFeedbacks);
    }

    public FeedbackResponseDTO mapToFeedbackResponse(ReportFeedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(feedback.getId());
        dto.setReportId(feedback.getReportId());
        dto.setFeedbackFrom(feedback.getFeedbackFrom());
        dto.setFeedbackTo(feedback.getFeedbackTo());
        dto.setFeedbackMessage(feedback.getFeedbackMessage());
        dto.setStatus(feedback.getStatus());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setAddressedAt(feedback.getAddressedAt());
        return dto;
    }
}