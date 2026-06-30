package com.neusoft.hospital.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.config.UserContext;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 患者端数据隔离：按当前登录账号关联的 patient 记录过滤
 */
@Component
@RequiredArgsConstructor
public class PatientScopeHelper {

    private final PatientMapper patientMapper;

    public boolean isPatientUser() {
        UserContext.LoginUser user = UserContext.get();
        return user != null && "patient".equals(user.getRole());
    }

    public Patient findCurrentPatient() {
        UserContext.LoginUser user = UserContext.get();
        if (user == null || user.getUserId() == null) {
            return null;
        }
        return patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, user.getUserId())
                .last("LIMIT 1"));
    }

    public Patient requireCurrentPatient() {
        Patient patient = findCurrentPatient();
        if (patient == null) {
            throw new BusinessException(404, "患者档案不存在，请先完善个人信息");
        }
        return patient;
    }

    /** 患者端列表查询用的 patientId；非患者角色返回 null 表示不过滤 */
    public Long scopedPatientId() {
        if (!isPatientUser()) {
            return null;
        }
        Patient patient = findCurrentPatient();
        return patient != null ? patient.getId() : -1L;
    }

    public <T> void applyPatientScope(LambdaQueryWrapper<T> wrapper, SFunction<T, ?> patientIdColumn) {
        Long patientId = scopedPatientId();
        if (patientId != null) {
            wrapper.eq(patientIdColumn, patientId);
        }
    }

    public void assertOwnsPatient(Long patientId) {
        if (!isPatientUser() || patientId == null) {
            return;
        }
        Long scoped = scopedPatientId();
        if (scoped == null || scoped < 0 || !scoped.equals(patientId)) {
            throw new BusinessException(403, "无权访问该患者数据");
        }
    }
}
