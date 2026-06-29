package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Payment;
import com.neusoft.hospital.entity.RegisterOrder;
import com.neusoft.hospital.mapper.PaymentMapper;
import com.neusoft.hospital.mapper.RegisterOrderMapper;
import com.neusoft.hospital.service.RegisterService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.PaymentDetailHelper;
import com.neusoft.hospital.service.support.RegisterConfigHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterOrderMapper registerOrderMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentDetailHelper paymentDetailHelper;
    private final RegisterConfigHelper registerConfigHelper;

    @Override
    public PageResult<RegisterOrder> list(PageQuery query) {
        return EntityPageHelper.page(registerOrderMapper, query, w -> {
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

        RegisterOrder order = new RegisterOrder();
        order.setRegisterNo("GH" + System.currentTimeMillis());
        order.setPatientName((String) body.get("patientName"));
        order.setDepartment((String) body.get("department"));
        order.setDoctorName((String) body.get("doctorName"));
        if (body.get("doctorId") != null) {
            order.setDoctorId(Long.valueOf(body.get("doctorId").toString()));
        }
        if (body.get("patientId") != null) {
            order.setPatientId(Long.valueOf(body.get("patientId").toString()));
        }
        order.setRegisterType(registerType);
        order.setFee(fee);
        order.setStatus(0);
        order.setRegisterTime(now);
        order.setCreateTime(now);
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
        paymentMapper.insert(payment);

        return order;
    }

    @Override
    public void cancel(Long id) {
        RegisterOrder order = registerOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "挂号记录不存在");
        }
        if (order.getStatus() != null && order.getStatus() == 3) {
            throw new BusinessException(409, "该号已退");
        }
        order.setStatus(3);
        registerOrderMapper.updateById(order);
    }

    private BigDecimal resolveRegisterFee(String registerType) {
        return registerConfigHelper.resolveFee(registerType);
    }
}
