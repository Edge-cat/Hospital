package com.neusoft.hospital.service;

import com.neusoft.hospital.dto.LoginRequest;
import com.neusoft.hospital.dto.LoginResponse;
import com.neusoft.hospital.dto.RegisterRequest;
import com.neusoft.hospital.dto.WxLoginRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, String ip);

    LoginResponse register(RegisterRequest request, String ip);

    LoginResponse wxLogin(WxLoginRequest request, String ip);
}
