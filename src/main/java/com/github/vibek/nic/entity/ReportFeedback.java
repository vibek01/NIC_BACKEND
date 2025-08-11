package com.github.vibek.nic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId; // References report.id

    @Column(name = "feedback_from", nullable = false)
    private UUID feedbackFrom; // Supervisor who gave feedback

    @Column(name = "feedback_to", nullable = false)
    private UUID feedbackTo; // Original report submitter

    @Column(name = "feedback_message", nullable = false, columnDefinition = "TEXT")
    private String feedbackMessage;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, ADDRESSED, RESOLVED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "addressed_at")
    private LocalDateTime addressedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
