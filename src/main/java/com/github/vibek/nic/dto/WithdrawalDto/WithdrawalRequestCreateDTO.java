package com.github.vibek.nic.dto.WithdrawalDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class WithdrawalRequestCreateDTO {
    @NotNull private UUID caseId;
    @NotNull private UUID requesterId;
    @NotBlank private String reason;
}