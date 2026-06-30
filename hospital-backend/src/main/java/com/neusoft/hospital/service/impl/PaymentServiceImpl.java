package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Payment;
import com.neusoft.hospital.mapper.PaymentMapper;
import com.neusoft.hospital.service.PaymentService;
import com.neusoft.hospital.service.OperationLogService;
import com.neusoft.hospital.service.support.ConsultationFlowHelper;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.FinanceLedgerHelper;
import com.neusoft.hospital.service.support.PatientScopeHelper;
import com.neusoft.hospital.service.support.PaymentDetailHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentDetailHelper paymentDetailHelper;
    private final FinanceLedgerHelper financeLedgerHelper;
    private final OperationLogService operationLogService;
    private final PatientScopeHelper patientScopeHelper;
    private final ConsultationFlowHelper consultationFlowHelper;

    @Override
    public PageResult<Map<String, Object>> list(PageQuery query) {
        PageResult<Payment> page = EntityPageHelper.page(paymentMapper, query, w -> {
            patientScopeHelper.applyPatientScope(w, Payment::getPatientId);
            EntityPageHelper.keywordLike(w, query.getKeyword(), Payment::getPatientName, Payment::getPaymentNo);
            if (query.getStatus() != null) {
                w.eq(Payment::getStatus, query.getStatus());
            }
        });
        List<Map<String, Object>> list = page.getList().stream().map(paymentDetailHelper::enrich).toList();
        return new PageResult<>(list, page.getTotal(), page.getPage(), page.getPageSize());
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException(404, "账单不存在");
        }
        patientScopeHelper.assertOwnsPatient(payment.getPatientId());
        return paymentDetailHelper.enrich(payment);
    }

    @Override
    public Map<String, Object> pay(Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException(404, "账单不存在");
        }
        patientScopeHelper.assertOwnsPatient(payment.getPatientId());
        if (payment.getStatus() != null && payment.getStatus() != 0) {
            throw new BusinessException(409, "该账单已支付");
        }
        payment.setStatus(1);
        payment.setPayMethod(body.get("payMethod") != null ? body.get("payMethod").toString() : "微信");
        payment.setPayTime(LocalDateTime.now());
        payment.setVoucherNo("PZ" + System.currentTimeMillis());
        paymentMapper.updateById(payment);
        recordPaymentLedger(payment);
        logPaymentAction(payment);
        unlockRecordIfNeeded(payment);
        return paymentDetailHelper.enrich(payment);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchPay(Map<String, Object> body) {
        List<Object> rawIds = (List<Object>) body.get("ids");
        String payMethod = body.get("payMethod") != null ? body.get("payMethod").toString() : "微信";
        if (rawIds == null || rawIds.isEmpty()) {
            throw new BusinessException(409, "所选账单不可支付");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> paid = new ArrayList<>();
        for (Object rawId : rawIds) {
            Long id = Long.valueOf(rawId.toString());
            Payment payment = paymentMapper.selectById(id);
            if (payment != null && payment.getStatus() != null && payment.getStatus() == 0) {
                patientScopeHelper.assertOwnsPatient(payment.getPatientId());
                payment.setStatus(1);
                payment.setPayMethod(payMethod);
                payment.setPayTime(now);
                payment.setVoucherNo("PZ" + System.currentTimeMillis() + payment.getId());
                paymentMapper.updateById(payment);
                recordPaymentLedger(payment);
                logPaymentAction(payment);
                unlockRecordIfNeeded(payment);
                paid.add(paymentDetailHelper.enrich(payment));
            }
        }
        if (paid.isEmpty()) {
            throw new BusinessException(409, "所选账单不可支付");
        }
        return Map.of("list", paid);
    }

    private void logPaymentAction(Payment payment) {
        String module = "register".equals(payment.getItemType()) ? "挂号" : "缴费";
        String detail = String.format("%s · %s · ¥%s · %s",
                payment.getPatientName(),
                payment.getDepartment() != null ? payment.getDepartment() : "-",
                payment.getAmount(),
                payment.getItemName());
        operationLogService.recordPatientAction("mini", module, "已完成支付", "/payment", detail);
    }

    private void recordPaymentLedger(Payment payment) {
        long accountId = "medicine".equals(payment.getItemType()) || "check".equals(payment.getItemType())
                ? FinanceLedgerHelper.OUTPATIENT_ACCOUNT_ID
                : FinanceLedgerHelper.OUTPATIENT_ACCOUNT_ID;
        financeLedgerHelper.recordIncome(
                accountId,
                payment.getAmount(),
                payment.getItemName() != null ? payment.getItemName() : "缴费收入",
                "payment",
                payment.getPaymentNo(),
                payment.getDepartment(),
                FinanceLedgerHelper.currentOperator()
        );
    }

    @Override
    public void refund(Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException(404, "账单不存在");
        }
        if (payment.getStatus() == null || payment.getStatus() != 1) {
            throw new BusinessException(409, "仅已支付账单可退款");
        }
        payment.setStatus(2);
        paymentMapper.updateById(payment);
        financeLedgerHelper.recordRefund(
                FinanceLedgerHelper.resolveAccountId(payment.getItemType()),
                payment.getAmount(),
                payment.getItemName() != null ? payment.getItemName() + "退款" : "缴费退款",
                "payment",
                payment.getPaymentNo(),
                payment.getDepartment(),
                FinanceLedgerHelper.currentOperator()
        );
    }

    @Override
    public Map<String, Object> summary() {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, 0);
        patientScopeHelper.applyPatientScope(wrapper, Payment::getPatientId);
        List<Payment> pending = paymentMapper.selectList(wrapper);
        BigDecimal total = pending.stream().map(Payment::getAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", pending.size());
        result.put("totalAmount", total);
        return result;
    }

    private void unlockRecordIfNeeded(Payment payment) {
        if (payment.getRecordId() == null) {
            return;
        }
        String type = payment.getItemType();
        if ("check".equals(type) || "medicine".equals(type)) {
            consultationFlowHelper.tryUnlockMedicalRecord(payment.getRecordId());
        }
    }
}
