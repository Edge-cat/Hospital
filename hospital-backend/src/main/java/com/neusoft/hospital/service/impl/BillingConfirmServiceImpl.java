package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.config.UserContext;
import com.neusoft.hospital.entity.Drug;
import com.neusoft.hospital.entity.MedicalRecord;
import com.neusoft.hospital.entity.MedicalService;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.entity.Payment;
import com.neusoft.hospital.entity.RegisterOrder;
import com.neusoft.hospital.mapper.DrugMapper;
import com.neusoft.hospital.mapper.MedicalRecordMapper;
import com.neusoft.hospital.mapper.MedicalServiceMapper;
import com.neusoft.hospital.mapper.PatientMapper;
import com.neusoft.hospital.mapper.PaymentMapper;
import com.neusoft.hospital.service.BillingConfirmService;
import com.neusoft.hospital.service.support.ConsultationFlowHelper;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.OrderItemsHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillingConfirmServiceImpl implements BillingConfirmService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MedicalRecordMapper medicalRecordMapper;
    private final PatientMapper patientMapper;
    private final PaymentMapper paymentMapper;
    private final DrugMapper drugMapper;
    private final MedicalServiceMapper medicalServiceMapper;
    private final OrderItemsHelper orderItemsHelper;
    private final ConsultationFlowHelper consultationFlowHelper;

    @Override
    public PageResult<Map<String, Object>> listPending(PageQuery query) {
        PageResult<MedicalRecord> page = EntityPageHelper.page(medicalRecordMapper, query, w -> {
            w.eq(MedicalRecord::getStatus, 0);
            w.isNotNull(MedicalRecord::getOrderItems);
            w.like(MedicalRecord::getOrderItems, "\"confirmStatus\":\"pending\"");
            EntityPageHelper.keywordLike(w, query.getKeyword(),
                    MedicalRecord::getPatientName, MedicalRecord::getDoctorName, MedicalRecord::getDepartment);
        });
        List<Map<String, Object>> list = page.getList().stream().map(this::toPendingSummary).toList();
        return new PageResult<>(list, page.getTotal(), page.getPage(), page.getPageSize());
    }

    @Override
    public Map<String, Object> getDetail(Long recordId) {
        MedicalRecord record = requirePendingRecord(recordId);
        Map<String, Object> order = orderItemsHelper.parse(record.getOrderItems());
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("recordId", record.getId());
        detail.put("patientId", record.getPatientId());
        detail.put("patientName", record.getPatientName());
        detail.put("doctorName", record.getDoctorName());
        detail.put("department", record.getDepartment());
        detail.put("diagnosis", record.getDiagnosis());
        detail.put("visitTime", record.getVisitTime());
        detail.put("presentIllness", order.getOrDefault("presentIllness", ""));
        detail.put("examNote", order.getOrDefault("examNote", ""));
        detail.put("exams", enrichExamPrices(orderItemsHelper.exams(order)));
        detail.put("drugs", enrichDrugPrices(orderItemsHelper.drugs(order)));
        return detail;
    }

    @Override
    @Transactional
    public void confirm(Long recordId, Map<String, Object> body) {
        MedicalRecord record = requirePendingRecord(recordId);
        Patient patient = patientMapper.selectById(record.getPatientId());
        if (patient == null) {
            throw new BusinessException(404, "患者不存在");
        }

        List<Map<String, Object>> examLines = normalizeLines(body.get("exams"), "check");
        List<Map<String, Object>> drugLines = normalizeLines(body.get("drugs"), "medicine");
        if (examLines.isEmpty() && drugLines.isEmpty()) {
            throw new BusinessException(409, "请至少确认一项检查或药品");
        }

        Map<String, Object> order = orderItemsHelper.parse(record.getOrderItems());
        order.put("confirmStatus", OrderItemsHelper.STATUS_CONFIRMED);
        order.put("confirmedAt", LocalDateTime.now().format(DT_FMT));
        order.put("confirmedBy", currentOperator());
        order.put("exams", examLines);
        order.put("drugs", drugLines);
        record.setOrderItems(orderItemsHelper.write(order));
        medicalRecordMapper.updateById(record);

        RegisterOrder registerOrder = consultationFlowHelper.findRegisterOrderByRecord(record);
        LocalDateTime now = LocalDateTime.now();

        if (!examLines.isEmpty()) {
            Payment checkPay = consultationFlowHelper.buildConsultationPayment(
                    patient, registerOrder, record.getId(), "检查费", "check",
                    examLines, "请按导引单前往相应科室", "缴费后前往检验科", now);
            paymentMapper.insert(checkPay);
        }
        if (!drugLines.isEmpty()) {
            Payment medPay = consultationFlowHelper.buildConsultationPayment(
                    patient, registerOrder, record.getId(), "药品费", "medicine",
                    drugLines, "请按医嘱服药", "取药请前往门诊药房1号窗口", now);
            paymentMapper.insert(medPay);
        }

        consultationFlowHelper.tryUnlockMedicalRecord(record.getId());
    }

    private MedicalRecord requirePendingRecord(Long recordId) {
        MedicalRecord record = medicalRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "病历不存在");
        }
        if (!orderItemsHelper.isPendingConfirm(record.getOrderItems())) {
            throw new BusinessException(409, "该医嘱已确认或不可重复确认");
        }
        return record;
    }

    private Map<String, Object> toPendingSummary(MedicalRecord record) {
        Map<String, Object> order = orderItemsHelper.parse(record.getOrderItems());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("recordId", record.getId());
        row.put("patientName", record.getPatientName());
        row.put("doctorName", record.getDoctorName());
        row.put("department", record.getDepartment());
        row.put("diagnosis", record.getDiagnosis());
        row.put("visitTime", record.getVisitTime());
        row.put("examCount", orderItemsHelper.exams(order).size());
        row.put("drugCount", orderItemsHelper.drugs(order).size());
        return row;
    }

    private List<Map<String, Object>> enrichExamPrices(List<Map<String, Object>> exams) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> exam : exams) {
            Map<String, Object> row = new LinkedHashMap<>(exam);
            String name = exam.get("name") != null ? exam.get("name").toString() : "";
            BigDecimal price = lookupServicePrice(name);
            row.put("unitPrice", price);
            row.put("qty", exam.getOrDefault("qty", 1));
            row.put("amount", price);
            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> enrichDrugPrices(List<Map<String, Object>> drugs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> drug : drugs) {
            Map<String, Object> row = new LinkedHashMap<>(drug);
            int qty = drug.get("qty") != null ? Integer.parseInt(drug.get("qty").toString()) : 1;
            BigDecimal unitPrice = lookupDrugPrice(drug);
            row.put("unitPrice", unitPrice);
            row.put("qty", qty);
            row.put("amount", unitPrice.multiply(BigDecimal.valueOf(qty)));
            result.add(row);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeLines(Object raw, String type) {
        List<Map<String, Object>> lines = new ArrayList<>();
        if (!(raw instanceof List<?> list)) {
            return lines;
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String name = map.get("name") != null ? map.get("name").toString().trim() : "";
            if (!StringUtils.hasText(name)) {
                continue;
            }
            int qty = map.get("qty") != null ? Integer.parseInt(map.get("qty").toString()) : 1;
            BigDecimal unitPrice = map.get("unitPrice") != null
                    ? new BigDecimal(map.get("unitPrice").toString())
                    : ("check".equals(type) ? lookupServicePrice(name) : lookupDrugPrice(map));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", name);
            row.put("qty", qty);
            row.put("unitPrice", unitPrice);
            row.put("amount", unitPrice.multiply(BigDecimal.valueOf(qty)));
            if (map.get("usage") != null) {
                row.put("usage", map.get("usage"));
            }
            lines.add(row);
        }
        return lines;
    }

    private BigDecimal lookupServicePrice(String name) {
        if (!StringUtils.hasText(name)) {
            return BigDecimal.ZERO;
        }
        MedicalService service = medicalServiceMapper.selectOne(new LambdaQueryWrapper<MedicalService>()
                .eq(MedicalService::getServiceName, name)
                .eq(MedicalService::getStatus, 1)
                .last("LIMIT 1"));
        if (service != null && service.getPrice() != null) {
            return service.getPrice();
        }
        return switch (name) {
            case "血常规" -> new BigDecimal("35");
            case "尿常规" -> new BigDecimal("25");
            case "X光胸片" -> new BigDecimal("80");
            case "B超" -> new BigDecimal("120");
            case "心电图" -> new BigDecimal("40");
            case "CT" -> new BigDecimal("200");
            default -> new BigDecimal("50");
        };
    }

    private BigDecimal lookupDrugPrice(Map<?, ?> drug) {
        if (drug.get("drugId") != null) {
            Drug entity = drugMapper.selectById(Long.valueOf(drug.get("drugId").toString()));
            if (entity != null && entity.getPrice() != null) {
                return entity.getPrice();
            }
        }
        String name = drug.get("name") != null ? drug.get("name").toString() : "";
        if (StringUtils.hasText(name)) {
            Drug entity = drugMapper.selectOne(new LambdaQueryWrapper<Drug>()
                    .like(Drug::getDrugName, name)
                    .eq(Drug::getStatus, 1)
                    .last("LIMIT 1"));
            if (entity != null && entity.getPrice() != null) {
                return entity.getPrice();
            }
        }
        return new BigDecimal("30");
    }

    private String currentOperator() {
        UserContext.LoginUser user = UserContext.get();
        return user != null && StringUtils.hasText(user.getName()) ? user.getName() : "护士";
    }
}
