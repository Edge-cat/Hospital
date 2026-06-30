package com.neusoft.hospital.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.entity.MedicalRecord;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.entity.Payment;
import com.neusoft.hospital.entity.RegisterOrder;
import com.neusoft.hospital.mapper.MedicalRecordMapper;
import com.neusoft.hospital.mapper.PaymentMapper;
import com.neusoft.hospital.mapper.RegisterOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生接诊完成后的挂号状态、病历归档与诊疗缴费联动。
 * 医生开单（无价格）→ 护士确认计价 → 患者缴费 → 病历解锁。
 */
@Component
@RequiredArgsConstructor
public class ConsultationFlowHelper {

    private final RegisterOrderMapper registerOrderMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentDetailHelper paymentDetailHelper;
    private final OrderItemsHelper orderItemsHelper;

    public void syncRegisterOnStart(Long patientId) {
        RegisterOrder order = findActiveRegisterOrder(patientId);
        if (order != null && order.getStatus() != null && order.getStatus() == 0) {
            order.setStatus(1);
            registerOrderMapper.updateById(order);
        }
    }

    public void completeConsultation(Patient patient, Map<String, Object> body) {
        RegisterOrder order = findActiveRegisterOrder(patient.getId());
        LocalDateTime now = LocalDateTime.now();

        String chiefComplaint = text(body, "chiefComplaint");
        String presentIllness = text(body, "presentIllness");
        String diagnosis = text(body, "diagnosis");
        String examNote = text(body, "examNote");
        if (!StringUtils.hasText(diagnosis)) {
            diagnosis = "待进一步检查";
        }

        List<Map<String, Object>> examItems = parseExamItems(body);
        List<Map<String, Object>> drugItems = parseDrugItems(body);

        Long doctorId = order != null ? order.getDoctorId() : null;
        String doctorName = order != null ? order.getDoctorName() : null;
        String department = order != null && StringUtils.hasText(order.getDepartment())
                ? order.getDepartment()
                : patient.getDepartment();

        Map<String, Object> orderItems = new LinkedHashMap<>();
        orderItems.put("confirmStatus", OrderItemsHelper.STATUS_PENDING);
        orderItems.put("presentIllness", presentIllness);
        orderItems.put("examNote", examNote);
        orderItems.put("exams", examItems);
        orderItems.put("drugs", drugItems);

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(patient.getId());
        record.setPatientName(patient.getName());
        record.setDoctorId(doctorId);
        record.setDoctorName(doctorName);
        record.setDepartment(department);
        record.setDiagnosis(diagnosis);
        record.setTreatment(buildTreatment(chiefComplaint, examItems, drugItems));
        record.setOrderItems(orderItemsHelper.write(orderItems));
        record.setVisitTime(now);
        record.setStatus(0);
        record.setRevisionStatus(0);
        record.setRevisions("[]");
        record.setCreateTime(now);
        medicalRecordMapper.insert(record);

        if (order != null) {
            order.setStatus(2);
            registerOrderMapper.updateById(order);
        }
    }

    public void tryUnlockMedicalRecord(Long recordId) {
        if (recordId == null) {
            return;
        }
        MedicalRecord record = medicalRecordMapper.selectById(recordId);
        if (record == null || record.getStatus() == null || record.getStatus() != 0) {
            return;
        }
        Long pending = paymentMapper.selectCount(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getRecordId, recordId)
                .eq(Payment::getStatus, 0)
                .in(Payment::getItemType, "check", "medicine"));
        if (pending == 0) {
            record.setStatus(2);
            medicalRecordMapper.updateById(record);
        }
    }

    /** 医生工作台：当日/最近一次就诊病历及锁定状态 */
    public Map<String, Object> getConsultationRecord(Long patientId, Integer patientStatus) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lockLevel", "none");
        result.put("lockReason", "");
        result.put("chiefComplaint", "");
        result.put("diagnosis", "");
        result.put("prescription", "");
        result.put("presentIllness", "");
        result.put("examNote", "");
        result.put("exams", List.of());
        result.put("drugs", List.of());

        if (patientStatus == null || patientStatus != 2) {
            return result;
        }

        MedicalRecord record = findLatestConsultationRecord(patientId);
        if (record == null) {
            return result;
        }

        Map<String, Object> order = orderItemsHelper.parse(record.getOrderItems());
        Map<String, String> parsed = parseTreatmentFields(record.getTreatment());
        result.put("recordId", record.getId());
        result.put("chiefComplaint", parsed.get("chiefComplaint"));
        result.put("diagnosis", record.getDiagnosis() != null ? record.getDiagnosis() : "");
        result.put("prescription", formatDrugLines(orderItemsHelper.drugs(order)));
        result.put("presentIllness", order.getOrDefault("presentIllness", ""));
        result.put("examNote", order.getOrDefault("examNote", ""));
        result.put("exams", orderItemsHelper.exams(order).stream().map(e -> e.get("name")).toList());
        result.put("drugs", orderItemsHelper.drugs(order));
        result.put("visitTime", record.getVisitTime());

        String confirmStatus = orderItemsHelper.confirmStatus(order);
        if (OrderItemsHelper.STATUS_PENDING.equals(confirmStatus)) {
            result.put("lockLevel", "semi");
            result.put("lockReason", "就诊已完成，待护士确认计价");
        } else if (record.getStatus() != null && record.getStatus() == 2) {
            result.put("lockLevel", "full");
            result.put("lockReason", "患者已缴费，病历已归档锁定");
        } else {
            result.put("lockLevel", "semi");
            result.put("lockReason", "护士已确认计价，待患者缴费");
        }
        return result;
    }

    public Payment buildConsultationPayment(Patient patient, RegisterOrder order, Long recordId,
                                            String itemName, String itemType,
                                            List<Map<String, Object>> breakdown, String advice,
                                            String guideTip, LocalDateTime now) {
        java.math.BigDecimal amount = breakdown.stream()
                .map(row -> row.get("amount"))
                .filter(v -> v != null)
                .map(v -> new java.math.BigDecimal(v.toString()))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Payment payment = new Payment();
        payment.setPaymentNo("JF" + System.currentTimeMillis() + ("check".equals(itemType) ? "C" : "M"));
        payment.setPatientId(patient.getId());
        payment.setPatientName(patient.getName());
        payment.setItemName(itemName);
        payment.setItemType(itemType);
        payment.setDepartment(order != null ? order.getDepartment() : patient.getDepartment());
        payment.setDoctorName(order != null ? order.getDoctorName() : null);
        payment.setAmount(amount);
        payment.setStatus(0);
        payment.setPayMethod("");
        payment.setAdvice(advice);
        payment.setGuideTip(guideTip);
        payment.setFeeBreakdown(paymentDetailHelper.toJson(breakdown));
        payment.setRecordId(recordId);
        if (order != null) {
            payment.setRegisterId(order.getId());
            payment.setRegisterNo(order.getRegisterNo());
        }
        payment.setCreateTime(now);
        return payment;
    }

    public RegisterOrder findRegisterOrderByRecord(MedicalRecord record) {
        Payment linked = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getRecordId, record.getId())
                .last("LIMIT 1"));
        if (linked != null && linked.getRegisterId() != null) {
            return registerOrderMapper.selectById(linked.getRegisterId());
        }
        return registerOrderMapper.selectOne(new LambdaQueryWrapper<RegisterOrder>()
                .eq(RegisterOrder::getPatientId, record.getPatientId())
                .eq(RegisterOrder::getStatus, 2)
                .orderByDesc(RegisterOrder::getId)
                .last("LIMIT 1"));
    }

    private MedicalRecord findLatestConsultationRecord(Long patientId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        MedicalRecord today = medicalRecordMapper.selectOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getPatientId, patientId)
                .ge(MedicalRecord::getVisitTime, todayStart)
                .orderByDesc(MedicalRecord::getVisitTime)
                .last("LIMIT 1"));
        if (today != null) {
            return today;
        }
        return medicalRecordMapper.selectOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getPatientId, patientId)
                .orderByDesc(MedicalRecord::getVisitTime)
                .last("LIMIT 1"));
    }

    public Map<String, String> parseTreatmentFields(String treatment) {
        Map<String, String> parsed = new LinkedHashMap<>();
        parsed.put("chiefComplaint", "");
        parsed.put("prescription", "");
        if (!StringUtils.hasText(treatment)) {
            return parsed;
        }
        String text = treatment.trim();
        java.util.regex.Matcher chiefMatch = java.util.regex.Pattern
                .compile("主诉[：:]\\s*([\\s\\S]*?)(?=\\n\\s*(?:检查项目|处方)[：:]|$)")
                .matcher(text);
        java.util.regex.Matcher rxMatch = java.util.regex.Pattern
                .compile("处方[：:]\\s*\\n?([\\s\\S]*)")
                .matcher(text);
        if (chiefMatch.find()) {
            parsed.put("chiefComplaint", chiefMatch.group(1).trim());
        }
        if (rxMatch.find()) {
            parsed.put("prescription", rxMatch.group(1).trim());
        }
        return parsed;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseExamItems(Map<String, Object> body) {
        List<Map<String, Object>> items = new ArrayList<>();
        Object raw = body.get("exams");
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                String name = item != null ? item.toString().trim() : "";
                if (StringUtils.hasText(name)) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", name);
                    items.add(row);
                }
            }
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseDrugItems(Map<String, Object> body) {
        List<Map<String, Object>> items = new ArrayList<>();
        Object raw = body.get("drugs");
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    String name = map.get("name") != null ? map.get("name").toString().trim() : "";
                    if (!StringUtils.hasText(name)) {
                        continue;
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", name);
                    if (map.get("drugId") != null) {
                        row.put("drugId", Long.valueOf(map.get("drugId").toString()));
                    }
                    row.put("qty", map.get("qty") != null ? Integer.valueOf(map.get("qty").toString()) : 1);
                    row.put("usage", map.get("usage") != null ? map.get("usage").toString() : "");
                    items.add(row);
                }
            }
        }
        String prescription = text(body, "prescription");
        if (items.isEmpty() && StringUtils.hasText(prescription)) {
            for (String line : prescription.split("\\n")) {
                String name = line.trim();
                if (StringUtils.hasText(name)) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", name);
                    row.put("qty", 1);
                    row.put("usage", "");
                    items.add(row);
                }
            }
        }
        return items;
    }

    private RegisterOrder findActiveRegisterOrder(Long patientId) {
        if (patientId == null) {
            return null;
        }
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        return registerOrderMapper.selectOne(new LambdaQueryWrapper<RegisterOrder>()
                .eq(RegisterOrder::getPatientId, patientId)
                .in(RegisterOrder::getStatus, 0, 1)
                .ge(RegisterOrder::getRegisterTime, todayStart)
                .le(RegisterOrder::getRegisterTime, todayEnd)
                .orderByDesc(RegisterOrder::getId)
                .last("LIMIT 1"));
    }

    private String buildTreatment(String chiefComplaint, List<Map<String, Object>> exams,
                                  List<Map<String, Object>> drugs) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(chiefComplaint)) {
            sb.append("主诉：").append(chiefComplaint.trim());
        }
        if (!exams.isEmpty()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append("检查项目：").append(String.join("、",
                    exams.stream().map(e -> e.get("name").toString()).toList()));
        }
        String drugText = formatDrugLines(drugs);
        if (StringUtils.hasText(drugText)) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append("处方：\n").append(drugText);
        }
        return sb.length() > 0 ? sb.toString() : "遵医嘱观察";
    }

    private String formatDrugLines(List<Map<String, Object>> drugs) {
        if (drugs == null || drugs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> drug : drugs) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(drug.get("name"));
            Object qty = drug.get("qty");
            if (qty != null && !"1".equals(qty.toString())) {
                sb.append(" ×").append(qty);
            }
            Object usage = drug.get("usage");
            if (usage != null && StringUtils.hasText(usage.toString())) {
                sb.append("  ").append(usage);
            }
        }
        return sb.toString();
    }

    private String text(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return "";
        }
        return body.get(key).toString().trim();
    }
}
