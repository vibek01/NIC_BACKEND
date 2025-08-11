package com.github.vibek.nic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.vibek.nic.enums.ReportStatus;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================ THE FIX: PART 1 ============================
    // We are replacing the raw UUID with a proper JPA relationship.
    // This tells Hibernate how the Report and ChildMarriageCase entities are connected.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private ChildMarriageCase caseEntity;
    // =======================================================================

    @Column(name = "person_id")
    private UUID personId;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "department")
    private String department;

    @Column(name = "report", nullable = false, columnDefinition = "TEXT")
    private String report;

    @Column(name = "is_final_report", nullable = false)
    private Boolean isFinalReport = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}