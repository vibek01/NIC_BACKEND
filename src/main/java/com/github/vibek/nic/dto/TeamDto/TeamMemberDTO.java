package com.github.vibek.nic.dto.TeamDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String role; // e.g., "MEMBER" or "SUPERVISOR"
    private String department;
    private String designation;
    private String phoneNumber;
}