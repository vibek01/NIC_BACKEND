package com.github.vibek.nic.dto.FeedbackDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
public class FeedbackResponseDTO {
    private Long id;
    private Long reportId;
    private UUID feedbackFrom;
    private String feedbackFromName; // Optional: supervisor name
    private UUID feedbackTo;
    private String feedbackToName; // Optional: submitter name
    private String feedbackMessage;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime addressedAt;

    // Optional: Include report details
    private String reportContent;
    private String department;
    private UUID caseId;
}
