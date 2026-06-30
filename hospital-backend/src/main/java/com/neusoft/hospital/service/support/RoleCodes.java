package com.neusoft.hospital.service.support;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.entity.SysUser;
import org.springframework.util.StringUtils;

/**
 * 管理端账号角色映射：roleKey → role + roleLabel
 */
public final class RoleCodes {

    private RoleCodes() {
    }

    public static void applyRoleKey(SysUser user) {
        if (user == null) {
            return;
        }
        if (StringUtils.hasText(user.getRoleKey())) {
            mapRoleKey(user, user.getRoleKey());
            return;
        }
        if (StringUtils.hasText(user.getRole())) {
            if (!StringUtils.hasText(user.getRoleLabel())) {
                user.setRoleLabel(defaultLabel(user.getRole()));
            }
            return;
        }
        throw new BusinessException(400, "请选择角色");
    }

    public static String toRoleKey(SysUser user) {
        if (user == null || !StringUtils.hasText(user.getRole())) {
            return "admin";
        }
        if ("admin".equals(user.getRole()) && "财务".equals(user.getRoleLabel())) {
            return "finance";
        }
        if ("nurse".equals(user.getRole()) && "药师".equals(user.getRoleLabel())) {
            return "pharmacy";
        }
        return user.getRole();
    }

    private static void mapRoleKey(SysUser user, String roleKey) {
        switch (roleKey) {
            case "admin" -> {
                user.setRole("admin");
                user.setRoleLabel("管理员");
            }
            case "doctor" -> {
                user.setRole("doctor");
                user.setRoleLabel("医生");
            }
            case "nurse" -> {
                user.setRole("nurse");
                user.setRoleLabel("护士");
            }
            case "finance" -> {
                user.setRole("admin");
                user.setRoleLabel("财务");
            }
            case "pharmacy" -> {
                user.setRole("nurse");
                user.setRoleLabel("药师");
            }
            default -> throw new BusinessException(400, "无效角色：" + roleKey);
        }
    }

    private static String defaultLabel(String role) {
        return switch (role) {
            case "admin" -> "管理员";
            case "doctor" -> "医生";
            case "nurse" -> "护士";
            case "patient" -> "患者";
            default -> role;
        };
    }
}
