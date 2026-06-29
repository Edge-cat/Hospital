package com.neusoft.hospital.service.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 挂号号别与费用 — 与 sys_config 及 RegisterServiceImpl 计费规则一致 */
@Component
@RequiredArgsConstructor
public class RegisterConfigHelper {

    private static final BigDecimal DEFAULT_NORMAL = new BigDecimal("10");
    private static final BigDecimal DEFAULT_EXPERT = new BigDecimal("50");

    private final SysConfigHelper configHelper;

    public BigDecimal resolveFee(String registerType) {
        if ("专家号".equals(registerType)) {
            return configHelper.getDecimal("expert_fee", DEFAULT_EXPERT);
        }
        if ("急诊号".equals(registerType)) {
            BigDecimal base = configHelper.getDecimal("register_fee", DEFAULT_NORMAL);
            return base.multiply(new BigDecimal("2"));
        }
        return configHelper.getDecimal("register_fee", DEFAULT_NORMAL);
    }

    public List<Map<String, Object>> listTypes() {
        BigDecimal normal = configHelper.getDecimal("register_fee", DEFAULT_NORMAL);
        BigDecimal expert = configHelper.getDecimal("expert_fee", DEFAULT_EXPERT);
        BigDecimal emergency = normal.multiply(new BigDecimal("2"));

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(type("普通号", normal));
        list.add(type("专家号", expert));
        list.add(type("急诊号", emergency));
        return list;
    }

    private Map<String, Object> type(String label, BigDecimal fee) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label);
        row.put("value", label);
        row.put("fee", fee);
        return row;
    }
}
