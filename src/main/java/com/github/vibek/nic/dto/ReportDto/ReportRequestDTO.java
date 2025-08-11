package com.github.vibek.nic.dto.ReportDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

import com.github.vibek.nic.enums.Department;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {

        @NotNull
        private UUID caseId;

        @NotNull
        private UUID personId;

        @NotBlank
        private String report;

        @NotBlank
        private String department;
    }
