package com.github.vibek.nic.dto.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PushTokenRequestDTO {
    @NotBlank(message = "pushToken is required")
    private String pushToken;
}