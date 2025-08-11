package com.github.vibek.nic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "team_formation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private ChildMarriageCase caseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Person supervisor;

    // ====================== MODIFICATION START ======================
    // Replaced the simple List<UUID> with a proper Many-to-Many relationship.
    // This tells Hibernate how to correctly join the team_formation and person tables.
    // The existing 'team_formation_member_ids' table will be used for this relationship.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "team_formation_member_ids",
        joinColumns = @JoinColumn(name = "team_formation_team_id"),
        inverseJoinColumns = @JoinColumn(name = "member_ids")
    )
    private List<Person> members = new ArrayList<>();
    // ======================= MODIFICATION END =======================

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Column(name = "formed_at")
    private LocalDateTime formedAt;

    @ElementCollection
    @CollectionTable(name = "team_formation_department_statuses", joinColumns = @JoinColumn(name = "team_formation_id"))
    @MapKeyColumn(name = "department")
    @Column(name = "status")
    private Map<String, String> departmentStatuses = new HashMap<>();

    @Column(name = "locked")
    private boolean locked = false;

    @ElementCollection
    @CollectionTable(name = "team_formation_department_members", joinColumns = @JoinColumn(name = "team_formation_id"))
    @MapKeyColumn(name = "department")
    @Column(name = "member_id")
    private Map<String, List<UUID>> departmentMembers = new HashMap<>();

    @PrePersist
    protected void onCreate() {
        notificationSentAt = LocalDateTime.now();
    }
}