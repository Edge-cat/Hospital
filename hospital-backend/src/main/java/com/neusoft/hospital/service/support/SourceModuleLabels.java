package com.neusoft.hospital.service.support;

import org.springframework.util.StringUtils;

public final class SourceModuleLabels {

    private SourceModuleLabels() {
    }

    public static String label(String sourceModule) {
        if (!StringUtils.hasText(sourceModule)) {
            return "手工录入";
        }
        return switch (sourceModule) {
            case "register" -> "挂号缴费";
            case "payment" -> "在线缴费";
            case "procurement" -> "采购管理";
            case "settlement" -> "结算管理";
            default -> sourceModule;
        };
    }

    public static String path(String sourceModule) {
        if (!StringUtils.hasText(sourceModule)) {
            return "/finance/income-expense";
        }
        return switch (sourceModule) {
            case "register" -> "/business/register";
            case "payment" -> "/business/payment";
            case "procurement" -> "/pharmacy/procurement";
            case "settlement" -> "/finance/settlement";
            default -> "/finance/income-expense";
        };
    }
}
