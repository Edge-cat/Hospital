package com.neusoft.hospital.service.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.hospital.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentDetailHelper {

    private final ObjectMapper objectMapper;

    public Map<String, Object> enrich(Payment payment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", payment.getId());
        map.put("paymentNo", payment.getPaymentNo());
        map.put("patientId", payment.getPatientId());
        map.put("patientName", payment.getPatientName());
        map.put("itemName", payment.getItemName());
        map.put("itemType", payment.getItemType() != null ? payment.getItemType() : resolveType(payment.getItemName()));
        map.put("department", payment.getDepartment());
        map.put("doctorName", payment.getDoctorName());
        map.put("amount", payment.getAmount());
        map.put("payMethod", payment.getPayMethod());
        map.put("status", payment.getStatus());
        map.put("payTime", payment.getPayTime());
        map.put("createTime", payment.getCreateTime());
        map.put("advice", payment.getAdvice() != null ? payment.getAdvice() :
                (payment.getStatus() != null && payment.getStatus() == 0
                        ? "缴费完成后请按导引单前往相应科室，如有疑问请咨询导诊台。"
                        : "缴费已完成，请保留凭证以备查验。"));
        map.put("guideTip", payment.getGuideTip() != null ? payment.getGuideTip() : "支持电子凭证，可在「我的-缴费记录」中查看。");
        map.put("feeBreakdown", parseBreakdown(payment));
        if (payment.getStatus() != null && payment.getStatus() == 1) {
            map.put("voucherNo", payment.getVoucherNo() != null ? payment.getVoucherNo()
                    : "PZ" + (payment.getPaymentNo() != null ? payment.getPaymentNo().substring(Math.max(0, payment.getPaymentNo().length() - 8)) : payment.getId()));
        }
        return map;
    }

    public String toJson(List<Map<String, Object>> breakdown) {
        try {
            return objectMapper.writeValueAsString(breakdown);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<Map<String, Object>> parseBreakdown(Payment payment) {
        if (payment.getFeeBreakdown() != null && !payment.getFeeBreakdown().isBlank()) {
            try {
                return objectMapper.readValue(payment.getFeeBreakdown(), new TypeReference<>() {});
            } catch (Exception ignored) {
            }
        }
        String type = resolveType(payment.getItemName());
        if ("check".equals(type) && payment.getAmount() != null) {
            return List.of(
                    Map.of("name", "血常规", "amount", payment.getAmount().multiply(new java.math.BigDecimal("0.15")).intValue()),
                    Map.of("name", "影像检查", "amount", payment.getAmount().multiply(new java.math.BigDecimal("0.85")).intValue())
            );
        }
        return List.of(Map.of("name", payment.getItemName(), "amount", payment.getAmount()));
    }

    private String resolveType(String itemName) {
        if (itemName == null) {
            return "register";
        }
        if (itemName.contains("检查")) {
            return "check";
        }
        if (itemName.contains("药")) {
            return "medicine";
        }
        return "register";
    }
}
