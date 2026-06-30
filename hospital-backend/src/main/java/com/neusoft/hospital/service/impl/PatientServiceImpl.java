package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.config.UserContext;
import com.neusoft.hospital.entity.MedicalRecord;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.entity.Payment;
import com.neusoft.hospital.mapper.MedicalRecordMapper;
import com.neusoft.hospital.mapper.PatientMapper;
import com.neusoft.hospital.mapper.PaymentMapper;
import com.neusoft.hospital.service.PatientService;
import com.neusoft.hospital.service.support.ConsultationFlowHelper;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PaymentMapper paymentMapper;
    private final ConsultationFlowHelper consultationFlowHelper;

    @Override
    public PageResult<Patient> list(PageQuery query) {
        return pagePatients(query);
    }

    @Override
    public PageResult<Patient> search(PageQuery query) {
        return pagePatients(query);
    }

    private PageResult<Patient> pagePatients(PageQuery query) {
        return EntityPageHelper.page(patientMapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Patient::getName, Patient::getPhone, Patient::getPatientNo);
            if (StringUtils.hasText(query.getDepartment())) {
                w.eq(Patient::getDepartment, query.getDepartment());
            }
            if (query.getStatus() != null) {
                w.eq(Patient::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Patient getById(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new BusinessException(404, "患者不存在");
        }
        return patient;
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        Patient patient = getById(id);
        List<MedicalRecord> visits = medicalRecordMapper.selectList(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getPatientName, patient.getName())
                .orderByDesc(MedicalRecord::getVisitTime)
                .last("LIMIT 8"));
        List<Payment> pays = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPatientName, patient.getName())
                .orderByDesc(Payment::getCreateTime));
        List<Payment> pending = pays.stream().filter(p -> p.getStatus() != null && p.getStatus() == 0).toList();
        List<Payment> paid = pays.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).toList();

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", patient.getId());
        detail.put("patientNo", patient.getPatientNo());
        detail.put("name", patient.getName());
        detail.put("gender", patient.getGender());
        detail.put("age", patient.getAge());
        detail.put("phone", patient.getPhone());
        detail.put("idCard", patient.getIdCard());
        detail.put("cardNo", patient.getCardNo());
        detail.put("address", patient.getAddress());
        detail.put("department", patient.getDepartment());
        detail.put("status", patient.getStatus());
        detail.put("allergyHistory", patient.getAllergyHistory());
        detail.put("chronicDisease", patient.getChronicDisease());
        detail.put("createTime", patient.getCreateTime());

        List<Map<String, Object>> medicalHistory = new ArrayList<>();
        for (MedicalRecord v : visits.stream().limit(5).toList()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", v.getVisitTime() != null ? v.getVisitTime().toLocalDate().toString() : null);
            item.put("content", v.getDiagnosis() + "（" + v.getDepartment() + "）");
            medicalHistory.add(item);
        }
        detail.put("medicalHistory", medicalHistory);
        detail.put("visitRecords", visits);

        Map<String, Object> paymentSummary = new LinkedHashMap<>();
        paymentSummary.put("pendingAmount", pending.stream().map(Payment::getAmount).filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        paymentSummary.put("paidAmount", paid.stream().map(Payment::getAmount).filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        paymentSummary.put("pendingCount", pending.size());
        paymentSummary.put("paidCount", paid.size());
        detail.put("paymentSummary", paymentSummary);
        detail.put("recentPayments", pays.stream().limit(5).toList());
        return detail;
    }

    @Override
    public Patient create(Patient patient) {
        if (patient.getPatientNo() == null) {
            patient.setPatientNo("P" + System.currentTimeMillis() % 100000000L);
        }
        if (patient.getStatus() == null) {
            patient.setStatus(0);
        }
        patient.setCreateTime(LocalDateTime.now());
        patientMapper.insert(patient);
        return patient;
    }

    @Override
    public void update(Long id, Patient patient) {
        getById(id);
        patient.setId(id);
        patientMapper.updateById(patient);
    }

    @Override
    public void delete(Long id) {
        patientMapper.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            patientMapper.deleteByIds(ids);
        }
    }

    @Override
    @Transactional
    public void startConsultation(Long id) {
        Patient patient = getById(id);
        patient.setStatus(1);
        patientMapper.updateById(patient);
        consultationFlowHelper.syncRegisterOnStart(id);
    }

    @Override
    @Transactional
    public void finishConsultation(Long id, Map<String, Object> body) {
        Patient patient = getById(id);
        if (patient.getStatus() != null && patient.getStatus() == 2) {
            throw new BusinessException(409, "该患者已完成就诊，病历不可修改");
        }
        patient.setStatus(2);
        patientMapper.updateById(patient);
        consultationFlowHelper.completeConsultation(patient, body);
    }

    @Override
    public Map<String, Object> getConsultationRecord(Long id) {
        Patient patient = getById(id);
        return consultationFlowHelper.getConsultationRecord(patient.getId(), patient.getStatus());
    }

    @Override
    public void joinQueue(Long id) {
        Patient patient = getById(id);
        patient.setStatus(0);
        patientMapper.updateById(patient);
    }

    @Override
    public Map<String, Object> getLoggedInPatientInfo() {
        UserContext.LoginUser loginUser = UserContext.get();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException(401, "未登录");
        }
        Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, loginUser.getUserId()));
        if (patient == null) {
            throw new BusinessException(404, "患者档案不存在");
        }
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", patient.getName());
        info.put("phone", patient.getPhone());
        info.put("idCard", patient.getIdCard());
        info.put("cardNo", patient.getCardNo() != null ? patient.getCardNo() : patient.getPatientNo());
        info.put("gender", patient.getGender());
        info.put("allergyHistory", patient.getAllergyHistory());
        info.put("chronicDisease", patient.getChronicDisease());
        info.put("address", patient.getAddress());
        return info;
    }
}
