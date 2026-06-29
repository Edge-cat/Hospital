package com.neusoft.hospital.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
