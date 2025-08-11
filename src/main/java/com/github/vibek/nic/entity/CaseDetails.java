// src/main/java/com/github/vibek/nic/entity/CaseDetails.java
package com.github.vibek.nic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "case_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    private ChildMarriageCase caseId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "case_details_department_members", joinColumns = @JoinColumn(name = "case_details_id"))
    @MapKeyColumn(name = "department")
    @Column(name = "member_id")
    private Map<String, List<UUID>> departmentMembers = new HashMap<>();

    @Column(name = "supervisor_id")
    private UUID supervisorId;

    @Column(name = "marriage_date")
    private LocalDateTime marriageDate;

    @Column(name = "boy_name")
    private String boyName;

    @Column(name = "boy_father_name")
    private String boyFatherName;

    @Column(name = "boy_address")
    private String boyAddress;

    // =================================================================
    // MODIFIED: Replaced String fields with Entity Relationships
    // =================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boy_subdivision_id")
    private Subdivision boySubdivision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "girl_subdivision_id")
    private Subdivision girlSubdivision;
    // =================================================================

    @Column(name = "girl_name")
    private String girlName;

    @Column(name = "girl_father_name")
    private String girlFatherName;

    @Column(name = "girl_address")
    private String girlAddress;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "marriage_address")
    private String marriageAddress;

    @Column(name = "marriage_location_landmark")
    private String marriagelocationlandmark;

    @Column(name = "police_station_near_marriage_location")
    private String policeStationNearMarriageLocation;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}