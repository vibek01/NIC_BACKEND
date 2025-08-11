package com.github.vibek.nic.dto.LoginDto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}