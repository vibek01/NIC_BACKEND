package com.github.vibek.nic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "response", nullable = false)
    private String response;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}