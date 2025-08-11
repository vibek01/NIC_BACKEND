// src/main/java/com/github/vibek/nic/entity/Person.java
package com.github.vibek.nic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.vibek.nic.enums.Role;

@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "department")
    private String department;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "push_token")
    private String pushToken;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "address", nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = true)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "password", nullable = false)
    private String password;

    // =================================================================
    // MODIFIED: Replaced String fields with Entity Relationships
    // =================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subdivision_id")
    private Subdivision subdivision;
    // =================================================================

    @Column(name = "office_name")
    private String officeName;

    @Column(name = "status")
    private String status;

    @Column(name = "designation")
    private String designation;

    @Column(name = "post_name")
    private String postName;

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