// File: src/main/java/com/github/vibek/nic/dto/WithdrawalDto/WithdrawalRequestResponseDTO.java
package com.github.vibek.nic.dto.WithdrawalDto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WithdrawalRequestResponseDTO {
    private UUID requestId;
    private UUID caseId;
    private UUID requesterId;
    private String requesterName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}