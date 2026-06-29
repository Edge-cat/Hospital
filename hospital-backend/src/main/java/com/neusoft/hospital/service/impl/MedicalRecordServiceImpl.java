package com.neusoft.hospital.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.neusoft.hospital.common.BusinessException;

import com.neusoft.hospital.common.PageQuery;

import com.neusoft.hospital.common.PageResult;

import com.neusoft.hospital.entity.MedicalRecord;

import com.neusoft.hospital.entity.Payment;

import com.neusoft.hospital.mapper.MedicalRecordMapper;

import com.neusoft.hospital.mapper.PaymentMapper;

import com.neusoft.hospital.service.MedicalRecordService;

import com.neusoft.hospital.service.support.EntityPageHelper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



@Service

@RequiredArgsConstructor

public class MedicalRecordServiceImpl implements MedicalRecordService {



    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    private final MedicalRecordMapper medicalRecordMapper;

    private final PaymentMapper paymentMapper;

    private final ObjectMapper objectMapper;



    @Override

    public PageResult<MedicalRecord> list(PageQuery query) {

        return EntityPageHelper.page(medicalRecordMapper, query, w -> {

            EntityPageHelper.keywordLike(w, query.getKeyword(), MedicalRecord::getPatientName, MedicalRecord::getDiagnosis);

            if (StringUtils.hasText(query.getDepartment())) {

                w.eq(MedicalRecord::getDepartment, query.getDepartment());

            }

            if (query.getStatus() != null) {

                w.eq(MedicalRecord::getStatus, query.getStatus());

            }

        });

    }



    @Override

    public MedicalRecord getById(Long id) {

        MedicalRecord record = medicalRecordMapper.selectById(id);

        if (record == null) {

            throw new BusinessException(404, "记录不存在");

        }

        return record;

    }



    @Override

    public void create(MedicalRecord record) {

        if (record.getVisitTime() == null) {

            record.setVisitTime(LocalDateTime.now());

        }

        if (record.getStatus() == null) {

            record.setStatus(2);

        }

        if (record.getRevisionStatus() == null) {

            record.setRevisionStatus(0);

        }

        if (record.getRevisions() == null) {

            record.setRevisions("[]");

        }

        record.setCreateTime(LocalDateTime.now());

        medicalRecordMapper.insert(record);

    }



    @Override

    public void update(Long id, MedicalRecord record) {

        getById(id);

        record.setId(id);

        medicalRecordMapper.updateById(record);

    }



    @Override

    public void delete(Long id) {

        medicalRecordMapper.deleteById(id);

    }



    @Override

    public Map<String, Object> getChain(Long id) {

        MedicalRecord record = getById(id);

        List<Payment> pays = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()

                .eq(Payment::getPatientName, record.getPatientName())

                .orderByDesc(Payment::getPayTime)

                .last("LIMIT 2"));

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", record.getId());

        result.put("patientId", record.getPatientId());

        result.put("patientName", record.getPatientName());

        result.put("doctorId", record.getDoctorId());

        result.put("doctorName", record.getDoctorName());

        result.put("department", record.getDepartment());

        result.put("diagnosis", record.getDiagnosis());

        result.put("treatment", record.getTreatment());

        result.put("visitTime", record.getVisitTime());

        result.put("status", record.getStatus());

        result.put("revisionStatus", record.getRevisionStatus());

        result.put("createTime", record.getCreateTime());



        List<Map<String, Object>> steps = new ArrayList<>();

        steps.add(step("初诊挂号", record.getVisitTime(), record.getDepartment() + " · 普通号", "已完成", "success", "primary"));

        steps.add(step("医生接诊", record.getVisitTime(), "主治 " + record.getDoctorName(), "已完成", "success", "success"));

        steps.add(step("诊断开方", record.getVisitTime(), record.getDiagnosis(), "已归档", "info", "warning"));

        Payment pay = pays.isEmpty() ? null : pays.get(0);

        steps.add(step("处方缴费", pay != null ? pay.getPayTime() : record.getVisitTime(),

                pay != null ? "已缴 ¥" + pay.getAmount() : "待缴费",

                pay != null ? "已支付" : "待支付",

                pay != null ? "success" : "warning", "danger"));

        result.put("steps", steps);

        result.put("revisions", parseRevisions(record.getRevisions()));

        return result;

    }



    @Override

    public void revise(Long id, Map<String, Object> body) {

        MedicalRecord record = getById(id);

        if (body.get("diagnosis") != null) {

            record.setDiagnosis(body.get("diagnosis").toString());

        }

        if (body.get("treatment") != null) {

            record.setTreatment(body.get("treatment").toString());

        }

        List<Map<String, Object>> revisions = parseRevisions(record.getRevisions());

        Map<String, Object> entry = new LinkedHashMap<>();

        entry.put("time", LocalDateTime.now().format(DT_FMT));

        entry.put("reason", body.get("reason"));

        entry.put("operator", "系统管理员");

        revisions.add(entry);

        record.setRevisions(writeRevisions(revisions));

        record.setRevisionStatus(1);

        medicalRecordMapper.updateById(record);

    }



    @Override

    public void withdraw(Long id, Map<String, Object> body) {

        MedicalRecord record = getById(id);

        record.setStatus(3);

        record.setRevisionStatus(2);

        List<Map<String, Object>> revisions = parseRevisions(record.getRevisions());

        Map<String, Object> entry = new LinkedHashMap<>();

        entry.put("time", LocalDateTime.now().format(DT_FMT));

        entry.put("reason", "撤回申请：" + (body.get("reason") != null ? body.get("reason") : ""));

        entry.put("operator", "系统管理员");

        revisions.add(entry);

        record.setRevisions(writeRevisions(revisions));

        medicalRecordMapper.updateById(record);

    }



    private List<Map<String, Object>> parseRevisions(String json) {

        if (json == null || json.isBlank()) {

            return new ArrayList<>();

        }

        try {

            return objectMapper.readValue(json, new TypeReference<>() {});

        } catch (Exception e) {

            return new ArrayList<>();

        }

    }



    private String writeRevisions(List<Map<String, Object>> revisions) {

        try {

            return objectMapper.writeValueAsString(revisions);

        } catch (Exception e) {

            return "[]";

        }

    }



    private Map<String, Object> step(String title, Object time, String content, String status, String tagType, String type) {

        Map<String, Object> step = new LinkedHashMap<>();

        step.put("title", title);

        step.put("time", time);

        step.put("content", content);

        step.put("status", status);

        step.put("tagType", tagType);

        step.put("type", type);

        return step;

    }

}

