package com.neusoft.hospital.config;

import lombok.Data;

public final class UserContext {

    private static final ThreadLocal<LoginUser> CURRENT = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        CURRENT.set(user);
    }

    public static LoginUser get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    @Data
    public static class LoginUser {
        private Long userId;
        private String username;
        private String name;
        private String role;
        private String roleLabel;
        private String department;
    }
}
