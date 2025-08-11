// File: src/main/java/com/github/vibek/nic/dto/WithdrawalDto/WithdrawalReassignDTO.java
package com.github.vibek.nic.dto.WithdrawalDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class WithdrawalReassignDTO {
    @NotNull private UUID newMemberId;
    @NotNull private UUID supervisorId;
}