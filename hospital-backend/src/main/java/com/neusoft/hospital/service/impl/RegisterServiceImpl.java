package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Payment;
import com.neusoft.hospital.entity.Patient;
import com.neusoft.hospital.entity.RegisterOrder;
import com.neusoft.hospital.mapper.PatientMapper;
import com.neusoft.hospital.mapper.PaymentMapper;
import com.neusoft.hospital.mapper.RegisterOrderMapper;
import com.neusoft.hospital.service.RegisterService;
import com.neusoft.hospital.service.OperationLogService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.OrderContextHelper;
import com.neusoft.hospital.service.support.PatientScopeHelper;
import com.neusoft.hospital.service.support.PaymentDetailHelper;
import com.neusoft.hospital.service.support.RegisterConfigHelper;
import com.neusoft.hospital.service.support.RegisterSlotHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterOrderMapper registerOrderMapper;
    private final PaymentMapper paymentMapper;
    private final PatientMapper patientMapper;
    private final PaymentDetailHelper paymentDetailHelper;
    private final RegisterConfigHelper registerConfigHelper;
    private final OrderContextHelper orderContextHelper;
    private final RegisterSlotHelper registerSlotHelper;
    private final OperationLogService operationLogService;
    private final PatientScopeHelper patientScopeHelper;

    @Override
    public PageResult<RegisterOrder> list(PageQuery query) {
        return EntityPageHelper.page(registerOrderMapper, query, w -> {
            patientScopeHelper.applyPatientScope(w, RegisterOrder::getPatientId);
            EntityPageHelper.keywordLike(w, query.getKeyword(), RegisterOrder::getPatientName, RegisterOrder::getRegisterNo);
            if (StringUtils.hasText(query.getDepartment())) {
                w.eq(RegisterOrder::getDepartment, query.getDepartment());
            }
            if (query.getStatus() != null) {
                w.eq(RegisterOrder::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public RegisterOrder create(Map<String, Object> body) {
        String registerType = body.get("registerType") != null ? body.get("registerType").toString() : "普通号";
        BigDecimal fee = resolveRegisterFee(registerType);
        LocalDateTime now = LocalDateTime.now();
        Long patientId = orderContextHelper.resolvePatientId(body);
        if (patientScopeHelper.isPatientUser()) {
            Patient current = patientScopeHelper.requireCurrentPatient();
            patientId = current.getId();
            if (!StringUtils.hasText((String) body.get("patientName"))) {
                body.put("patientName", current.getName());
            }
        }
        Long doctorId = orderContextHelper.resolveDoctorId(body);
        String timeSlot = body.get("timeSlot") != null ? body.get("timeSlot").toString() : null;

        if (patientId != null && doctorId != null && StringUtils.hasText(timeSlot)) {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
            RegisterOrder existing = registerOrderMapper.selectOne(new LambdaQueryWrapper<RegisterOrder>()
                    .eq(RegisterOrder::getPatientId, patientId)
                    .eq(RegisterOrder::getDoctorId, doctorId)
                    .eq(RegisterOrder::getTimeSlot, timeSlot)
                    .ge(RegisterOrder::getRegisterTime, todayStart)
                    .le(RegisterOrder::getRegisterTime, todayEnd)
                    .ne(RegisterOrder::getStatus, 3)
                    .orderByDesc(RegisterOrder::getId)
                    .last("LIMIT 1"));
            if (existing != null) {
                Payment existingPay = registerSlotHelper.findRegisterPayment(existing);
                if (existingPay != null && existingPay.getStatus() != null && existingPay.getStatus() == 0) {
                    return existing;
                }
            }
        }

        RegisterOrder order = new RegisterOrder();
        order.setRegisterNo("GH" + System.currentTimeMillis());
        order.setPatientName((String) body.get("patientName"));
        order.setDepartment((String) body.get("department"));
        order.setDoctorName((String) body.get("doctorName"));
        order.setDoctorId(doctorId);
        order.setPatientId(patientId);
        order.setRegisterType(registerType);
        order.setFee(fee);
        order.setStatus(0);
        order.setRegisterTime(now);
        order.setCreateTime(now);
        if (timeSlot != null) {
            order.setTimeSlot(timeSlot);
        }

        registerOrderMapper.insert(order);

        Payment payment = new Payment();
        payment.setPaymentNo("JF" + System.currentTimeMillis());
        payment.setPatientId(order.getPatientId());
        payment.setPatientName(order.getPatientName());
        payment.setItemName("挂号费");
        payment.setItemType("register");
        payment.setDepartment(order.getDepartment());
        payment.setDoctorName(order.getDoctorName());
        payment.setAmount(fee);
        payment.setStatus(0);
        payment.setPayMethod("");
        payment.setCreateTime(now);
        payment.setFeeBreakdown(paymentDetailHelper.toJson(List.of(Map.of("name", "挂号费", "amount", fee))));
        payment.setRegisterId(order.getId());
        payment.setRegisterNo(order.getRegisterNo());
        paymentMapper.insert(payment);

        enqueuePatientForConsultation(patientId, order.getDepartment());

        operationLogService.recordPatientAction("mini", "挂号", "提交了挂号申请", "/register",
                formatRegisterDetail(order));

        return order;
    }

    @Override
    public void cancel(Long id) {
        RegisterOrder order = registerOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "挂号记录不存在");
        }
        patientScopeHelper.assertOwnsPatient(order.getPatientId());
        if (order.getStatus() != null && order.getStatus() == 3) {
            throw new BusinessException(409, "该号已退");
        }
        Payment payment = registerSlotHelper.findRegisterPayment(order);
        if (payment != null && payment.getStatus() != null && payment.getStatus() == 1) {
            payment.setStatus(2);
            paymentMapper.updateById(payment);
        }
        order.setStatus(3);
        registerOrderMapper.updateById(order);
        if (order.getPatientId() != null) {
            Patient patient = patientMapper.selectById(order.getPatientId());
            if (patient != null && patient.getStatus() != null && patient.getStatus() == 0) {
                patient.setStatus(2);
                patientMapper.updateById(patient);
            }
        }
        operationLogService.recordPatientAction("mini", "挂号", "已取消挂号", "/register/" + id + "/cancel",
                formatRegisterDetail(order));
    }

    private String formatRegisterDetail(RegisterOrder order) {
        return String.format("%s · %s · %s%s",
                order.getPatientName(),
                order.getDepartment(),
                order.getDoctorName(),
                StringUtils.hasText(order.getTimeSlot()) ? " · " + order.getTimeSlot() : "");
    }

    private BigDecimal resolveRegisterFee(String registerType) {
        return registerConfigHelper.resolveFee(registerType);
    }

    /** 挂号成功后进入医生候诊队列（patient.status=0） */
    private void enqueuePatientForConsultation(Long patientId, String department) {
        if (patientId == null) {
            return;
        }
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            return;
        }
        patient.setStatus(0);
        if (StringUtils.hasText(department)) {
            patient.setDepartment(department);
        }
        patientMapper.updateById(patient);
    }
}
