package com.github.vibek.nic.dto.TeamDto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter @Setter
public class ManualTeamFormationRequest {

    @NotNull
    private UUID caseId;

    private UUID supervisorId;

    private Map<String, List<UUID>> departmentMembers;
}
