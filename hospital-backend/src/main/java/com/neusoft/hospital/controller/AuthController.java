package com.neusoft.hospital.controller;

import com.neusoft.hospital.common.ApiResponse;
import com.neusoft.hospital.dto.LoginRequest;
import com.neusoft.hospital.dto.LoginResponse;
import com.neusoft.hospital.dto.RegisterRequest;
import com.neusoft.hospital.dto.WxLoginRequest;
import com.neusoft.hospital.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ApiResponse.success(authService.login(request, ip), "登录成功");
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ApiResponse.success(authService.register(request, ip), "注册成功");
    }

    @PostMapping("/wx-login")
    public ApiResponse<LoginResponse> wxLogin(@RequestBody WxLoginRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        return ApiResponse.success(authService.wxLogin(request, ip), "微信登录成功");
    }
}
