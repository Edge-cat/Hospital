package com.neusoft.hospital.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    /** 手机号，同时作为登录用户名 */
    private String phone;
    private String password;
    private String name;
    private String idCard;
    private Integer gender;
}
