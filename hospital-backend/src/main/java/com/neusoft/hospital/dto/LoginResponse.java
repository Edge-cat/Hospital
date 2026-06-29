package com.neusoft.hospital.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String name;
        private String role;
        private String roleLabel;
        private String department;
        private java.util.List<Long> menuIds;
    }
}
