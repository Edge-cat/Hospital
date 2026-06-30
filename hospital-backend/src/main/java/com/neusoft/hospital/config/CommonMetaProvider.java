package com.neusoft.hospital.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CommonMetaProvider {

    private CommonMetaProvider() {
    }

    private static List<Map<String, Object>> opt(String... items) {
        return java.util.Arrays.stream(items)
                .map(item -> Map.<String, Object>of("label", item, "value", item))
                .toList();
    }

    private static List<Map<String, Object>> optKv(String label1, Object value1, String label2, Object value2) {
        return List.of(
                Map.of("label", label1, "value", value1),
                Map.of("label", label2, "value", value2));
    }

    public static Map<String, Object> meta() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("departments", opt("内科", "外科", "儿科", "骨科", "眼科", "皮肤科", "口腔科"));
        options.put("roles", opt("系统管理员", "医生", "护士", "财务", "药房"));
        options.put("staffRoles", List.of(
                Map.of("label", "系统管理员", "value", "admin"),
                Map.of("label", "医生", "value", "doctor"),
                Map.of("label", "护士", "value", "nurse"),
                Map.of("label", "财务", "value", "finance"),
                Map.of("label", "药房", "value", "pharmacy")));
        options.put("registerTypes", opt("普通号", "专家号", "急诊号"));
        options.put("payMethods", opt("现金", "微信", "支付宝", "银行卡"));
        options.put("paymentItems", opt("挂号费", "检查费", "药品费", "住院押金"));
        options.put("bedTypes", opt("普通床位", "VIP床位", "监护床位"));
        options.put("timeSlots", opt("08:00-09:00", "09:00-10:00", "10:00-11:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"));
        options.put("shiftTypes", opt("早班", "中班", "晚班"));
        options.put("noticeTypes", opt("通知", "公告", "紧急"));
        options.put("doctorTitles", opt("主任医师", "副主任医师", "主治医师", "住院医师"));
        options.put("logModules", opt("患者管理", "系统管理", "财务管理", "药房管理"));
        options.put("gender", optKv("男", 1, "女", 2));
        options.put("enableStatus", optKv("启用", 1, "禁用", 0));
        options.put("loginStatus", optKv("成功", 1, "失败", 0));

        Map<String, Object> enums = new LinkedHashMap<>();
        enums.put("registerStatus", enumMap(
                entry("0", "待就诊", "warning"),
                entry("1", "就诊中", "primary"),
                entry("2", "已完成", "success"),
                entry("3", "已退号", "info")));
        enums.put("appointmentStatus", enumMap(
                entry("0", "待确认", "warning"),
                entry("1", "已确认", "primary"),
                entry("2", "已完成", "success"),
                entry("3", "已取消", "info")));
        enums.put("paymentStatus", enumMap(
                entry("0", "待支付", "warning"),
                entry("1", "已支付", "success"),
                entry("2", "已退款", "info")));
        enums.put("bedStatus", enumMap(
                entry("0", "空闲", "success"),
                entry("1", "占用", "warning"),
                entry("2", "维护", "info")));
        enums.put("reimburseStatus", enumMap(
                entry("0", "待审批", "warning"),
                entry("1", "已通过", "success"),
                entry("2", "已驳回", "danger")));
        enums.put("consultationStatus", enumMap(
                entry("0", "待就诊", "info"),
                entry("1", "就诊中", "warning"),
                entry("2", "已完成", "success")));
        enums.put("orderStatus", enumMap(
                entry("0", "待处理", "info"),
                entry("1", "处理中", "warning"),
                entry("2", "已完成", "success"),
                entry("3", "已取消", "danger")));
        enums.put("noticeTypeTag", Map.of(
                "通知", Map.of("label", "通知", "type", "info"),
                "公告", Map.of("label", "公告", "type", "success"),
                "紧急", Map.of("label", "紧急", "type", "danger")));
        enums.put("reportStatus", enumMap(
                entry("0", "生成中", "warning"),
                entry("1", "已生成", "success")));

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("options", options);
        meta.put("enums", enums);
        return meta;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> options(String key) {
        Map<String, Object> options = (Map<String, Object>) meta().get("options");
        Object value = options.get(key);
        return value instanceof List ? (List<Map<String, Object>>) value : List.of();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> enums(String key) {
        Map<String, Object> enums = (Map<String, Object>) meta().get("enums");
        Object value = enums.get(key);
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    private static String[] entry(String key, String label, String type) {
        return new String[]{key, label, type};
    }

    private static Map<String, Object> enumMap(String[]... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String[] e : entries) {
            map.put(e[0], Map.of("label", e[1], "type", e[2]));
        }
        return map;
    }
}
