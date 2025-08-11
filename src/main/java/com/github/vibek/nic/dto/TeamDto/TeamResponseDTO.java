package com.github.vibek.nic.dto.TeamDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDTO {
    private UUID id;
    private UUID teamId;
    private UUID personId;
    private String personName;
    private String department;
    private String response;
    private LocalDateTime respondedAt;
    private UUID caseId;
}
