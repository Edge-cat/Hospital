package com.neusoft.hospital.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.hospital.common.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> ADMIN_ROLES = Set.of("admin");
    private static final Set<String> STAFF_ROLES = Set.of("admin", "doctor", "nurse");

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/wx-login") || path.startsWith("/api/common/")) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeError(response, HttpStatus.UNAUTHORIZED, 401, "未登录或令牌无效");
            return false;
        }
        String token = auth.substring(7);
        if (!jwtUtil.validateToken(token)) {
            writeError(response, HttpStatus.UNAUTHORIZED, 401, "令牌已过期或无效");
            return false;
        }
        Claims claims = jwtUtil.parseToken(token);
        UserContext.LoginUser user = new UserContext.LoginUser();
        user.setUserId(claims.get("userId", Long.class));
        user.setUsername(claims.getSubject());
        user.setRole(claims.get("role", String.class));
        user.setName(claims.get("name", String.class));
        UserContext.set(user);

        String role = user.getRole();
        if (path.startsWith("/api/admin/") && !ADMIN_ROLES.contains(role)) {
            writeError(response, HttpStatus.FORBIDDEN, 403, "无管理端访问权限");
            return false;
        }
        if (isStaffOnlyPath(path) && !STAFF_ROLES.contains(role)) {
            writeError(response, HttpStatus.FORBIDDEN, 403, "无权限访问该资源");
            return false;
        }
        return true;
    }

    private boolean isStaffOnlyPath(String path) {
        if (path.startsWith("/api/statistics/")) {
            return true;
        }
        if (path.startsWith("/api/finance/") || path.startsWith("/api/income-expense/")) {
            return true;
        }
        if (path.startsWith("/api/reimbursement/") || path.startsWith("/api/settlement/")) {
            return true;
        }
        if (path.startsWith("/api/procurement/") || path.startsWith("/api/inventory/")
                || path.startsWith("/api/dispensing/") || path.startsWith("/api/drug/")) {
            return true;
        }
        if (path.startsWith("/api/schedule/") && !path.contains("/calendar")) {
            return path.contains("/cancel") || path.matches(".*/api/schedule$") || path.matches(".*/api/schedule/\\d+$");
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, int code, String message) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(code, message));
    }
}
