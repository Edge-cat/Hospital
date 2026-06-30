package com.neusoft.hospital.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.config.UserContext;
import com.neusoft.hospital.entity.Doctor;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.mapper.DoctorMapper;
import com.neusoft.hospital.mapper.PatientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderContextHelper {

    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;

    public Long resolvePatientId(Map<String, Object> body) {
        if (body.get("patientId") != null) {
            return Long.valueOf(body.get("patientId").toString());
        }
        UserContext.LoginUser user = UserContext.get();
        if (user == null || user.getUserId() == null) {
            return null;
        }
        Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, user.getUserId())
                .last("LIMIT 1"));
        return patient != null ? patient.getId() : null;
    }

    public Long resolveDoctorId(Map<String, Object> body) {
        if (body.get("doctorId") != null) {
            return Long.valueOf(body.get("doctorId").toString());
        }
        String doctorName = (String) body.get("doctorName");
        if (!StringUtils.hasText(doctorName)) {
            return null;
        }
        LambdaQueryWrapper<Doctor> q = new LambdaQueryWrapper<Doctor>().eq(Doctor::getName, doctorName);
        String department = (String) body.get("department");
        if (StringUtils.hasText(department)) {
            q.eq(Doctor::getDepartment, department);
        }
        Doctor doctor = doctorMapper.selectOne(q.last("LIMIT 1"));
        return doctor != null ? doctor.getId() : null;
    }
}
