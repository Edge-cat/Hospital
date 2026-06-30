package com.neusoft.hospital.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.entity.Payment;
import com.neusoft.hospital.entity.RegisterOrder;
import com.neusoft.hospital.mapper.PaymentMapper;
import com.neusoft.hospital.mapper.RegisterOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 当日挂号号源：仅「已支付挂号费」的订单占用时段，同一挂号单只计一次。
 */
@Component
@RequiredArgsConstructor
public class RegisterSlotHelper {

    private final PaymentMapper paymentMapper;
    private final RegisterOrderMapper registerOrderMapper;

    public void applyPaidRegisterBookings(long doctorId, LocalDate start, LocalDate end,
                                            Map<String, Long> bookedCounts) {
        List<Payment> paidPayments = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getItemType, "register")
                .eq(Payment::getStatus, 1));
        Set<Long> countedRegisterIds = new HashSet<>();
        for (Payment payment : paidPayments) {
            RegisterOrder register = resolveRegisterOrder(payment);
            if (register == null || register.getId() == null) {
                continue;
            }
            if (!Objects.equals(register.getDoctorId(), doctorId)) {
                continue;
            }
            if (register.getStatus() != null && register.getStatus() == 3) {
                continue;
            }
            if (!countedRegisterIds.add(register.getId())) {
                continue;
            }
            if (register.getRegisterTime() == null || !StringUtils.hasText(register.getTimeSlot())) {
                continue;
            }
            LocalDate regDate = register.getRegisterTime().toLocalDate();
            if (regDate.isBefore(start) || regDate.isAfter(end)) {
                continue;
            }
            bookedCounts.merge(regDate + "|" + register.getTimeSlot(), 1L, Long::sum);
        }
    }

    /**
     * 仅通过 registerId / registerNo 精确关联，避免历史缴费单误匹配最新挂号。
     */
    public RegisterOrder resolveRegisterOrder(Payment payment) {
        if (payment.getRegisterId() != null) {
            RegisterOrder byId = registerOrderMapper.selectById(payment.getRegisterId());
            if (byId != null) {
                return byId;
            }
        }
        if (StringUtils.hasText(payment.getRegisterNo())) {
            return registerOrderMapper.selectOne(new LambdaQueryWrapper<RegisterOrder>()
                    .eq(RegisterOrder::getRegisterNo, payment.getRegisterNo())
                    .last("LIMIT 1"));
        }
        return null;
    }

    public Payment findRegisterPayment(RegisterOrder order) {
        if (order == null || order.getId() == null) {
            return null;
        }
        Payment byId = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getRegisterId, order.getId())
                .eq(Payment::getItemType, "register")
                .last("LIMIT 1"));
        if (byId != null) {
            return byId;
        }
        if (!StringUtils.hasText(order.getRegisterNo())) {
            return null;
        }
        return paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getRegisterNo, order.getRegisterNo())
                .eq(Payment::getItemType, "register")
                .last("LIMIT 1"));
    }
}
