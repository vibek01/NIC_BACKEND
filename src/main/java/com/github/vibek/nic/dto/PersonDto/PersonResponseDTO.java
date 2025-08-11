// src/main/java/com/github/vibek/nic/dto/PersonDto/PersonResponseDTO.java
package com.github.vibek.nic.dto.PersonDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String gender;
    private String address;
    private String role;
    private String department;
    private Integer rank;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // These fields remain as Strings for frontend compatibility
    private String district;
    private String designation;
    private String officeName;
    private String status;
    private String subdivision;
    private String postName;
}