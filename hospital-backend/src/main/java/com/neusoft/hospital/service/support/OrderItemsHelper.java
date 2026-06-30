package com.neusoft.hospital.service.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderItemsHelper {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";

    private final ObjectMapper objectMapper;

    public Map<String, Object> parse(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    public String write(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> exams(Map<String, Object> order) {
        if (order == null || order.get("exams") == null) {
            return new ArrayList<>();
        }
        return (List<Map<String, Object>>) order.get("exams");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> drugs(Map<String, Object> order) {
        if (order == null || order.get("drugs") == null) {
            return new ArrayList<>();
        }
        return (List<Map<String, Object>>) order.get("drugs");
    }

    public String confirmStatus(Map<String, Object> order) {
        if (order == null || order.get("confirmStatus") == null) {
            return "";
        }
        return order.get("confirmStatus").toString();
    }

    public boolean isPendingConfirm(String orderItemsJson) {
        return STATUS_PENDING.equals(confirmStatus(parse(orderItemsJson)));
    }
}
