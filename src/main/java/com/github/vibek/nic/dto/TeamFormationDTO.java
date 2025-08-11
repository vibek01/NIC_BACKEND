package com.github.vibek.nic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamFormationDTO {
    private UUID caseId;
    private UUID supervisorId;
    private Map<String, List<UUID>> departmentMembers;  // New: dynamic map
    private LocalDateTime formedAt;
    private Map<String, String> departmentStatuses;  // New: dynamic statuses
    // Remove fixed fields like policeMembers, policeStatus, etc.
}
