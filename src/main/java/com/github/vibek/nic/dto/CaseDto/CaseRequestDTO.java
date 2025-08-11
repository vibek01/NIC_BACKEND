package com.github.vibek.nic.dto.CaseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseRequestDTO {
    private String complainantPhone;
    private LocalDateTime reportedAt;
    private String status;
    private CaseDetailsDTO caseDetails;
}