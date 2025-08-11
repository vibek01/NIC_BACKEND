// src/main/java/com/github/vibek/nic/dto/PersonDto/PersonRequestDTO.java
package com.github.vibek.nic.dto.PersonDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequestDTO {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String gender;

    private String address;

    @NotBlank(message = "Role is required")
    private String role;

    private String department;

    @NotBlank(message = "Password is required")
    private String password;

    // These fields remain as Strings for frontend compatibility
    private String district;
    private String designation;
    private String officeName;
    private String status;
    private String subdivision;
    private Integer rank;
    private String postName;
}