package com.github.vibek.nic.dto.FeedbackDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FeedbackRequestDTO {
    @NotNull
    private Long reportId;

    @NotNull
    private UUID supervisorId; // Who's giving feedback

    @NotBlank
    private String feedbackMessage;
}
