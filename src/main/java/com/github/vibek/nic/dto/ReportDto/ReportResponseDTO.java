package com.github.vibek.nic.dto.ReportDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.vibek.nic.enums.ReportStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private Long id;
    private UUID caseId;
    private UUID personId;
    private String report;
    private String department;
    private LocalDateTime submittedAt;
    private Boolean isFinalReport;
    private ReportStatus status; // Add the status field
}