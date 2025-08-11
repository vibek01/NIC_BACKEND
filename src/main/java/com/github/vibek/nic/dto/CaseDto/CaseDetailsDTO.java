package com.github.vibek.nic.dto.CaseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.*;

import com.github.vibek.nic.dto.TeamDto.TeamMemberDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseDetailsDTO {
    private UUID id;
    private UUID caseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ✅ 2. Replace the old ID-based fields...
    // private Map<String, List<UUID>> departmentMembers;
    // private UUID supervisorId;

    // ✅ ...with our new, rich list of team member objects.
    private List<TeamMemberDTO> teamMembers;
    
    private LocalDateTime marriageDate;
    private String boyName;
    private String boyFatherName;
    private String boyAddress;
    private String boySubdivision;
    private String girlName;
    private String girlFatherName;
    private String girlAddress;
    private String girlSubdivision;
    private UUID teamId;
    private String marriageAddress;
    private String marriageLandmark;
    private String policeStationNearMarriageLocation;
}