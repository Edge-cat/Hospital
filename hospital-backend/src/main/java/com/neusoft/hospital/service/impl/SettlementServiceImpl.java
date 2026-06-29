package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Settlement;
import com.neusoft.hospital.mapper.SettlementMapper;
import com.neusoft.hospital.service.SettlementService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.FinanceLedgerHelper;
import com.neusoft.hospital.service.support.JsonStoreHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementMapper mapper;
    private final JsonStoreHelper jsonStoreHelper;
    private final FinanceLedgerHelper financeLedgerHelper;

    @Override
    public PageResult<Settlement> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Settlement::getPatientName);
            if (query.getStatus() != null) {
                w.eq(Settlement::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Settlement getById(Long id) {
        Settlement entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Settlement entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        if (!StringUtils.hasText(entity.getItemsJson())) {
            entity.setItemsJson(jsonStoreHelper.toJson(defaultItems(entity)));
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Settlement entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        Settlement item = getById(id);
        BigDecimal total = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = item.getPaidAmount() != null ? item.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal unpaid = total.subtract(paid);
        List<Map<String, Object>> feeItems = jsonStoreHelper.readList(item.getItemsJson());
        if (feeItems.isEmpty()) {
            feeItems = defaultItems(item);
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", item.getId());
        detail.put("settlementNo", item.getSettlementNo());
        detail.put("patientName", item.getPatientName());
        detail.put("totalAmount", total);
        detail.put("paidAmount", paid);
        detail.put("unpaid", unpaid);
        detail.put("status", item.getStatus());
        detail.put("items", feeItems);
        detail.put("feeItems", feeItems);
        detail.put("invoiceNo", item.getInvoiceNo());
        detail.put("insuranceAmount", total.multiply(new BigDecimal("0.6")));
        detail.put("selfPayAmount", unpaid.max(BigDecimal.ZERO));
        detail.put("paymentChannels", List.of("医保统筹", "微信", "银行卡"));
        return detail;
    }

    @Override
    public Map<String, Object> settle(Long id, Map<String, Object> body) {
        Settlement item = getById(id);
        item.setStatus(1);
        item.setPaidAmount(item.getTotalAmount());
        item.setSettleTime(LocalDateTime.now());
        item.setInvoiceNo("INV" + System.currentTimeMillis());
        mapper.updateById(item);

        financeLedgerHelper.recordIncome(
                FinanceLedgerHelper.INPATIENT_ACCOUNT_ID,
                item.getTotalAmount(),
                "住院结算",
                "settlement",
                item.getSettlementNo(),
                "住院部",
                FinanceLedgerHelper.currentOperator()
        );

        return Map.of(
                "invoiceNo", item.getInvoiceNo(),
                "settlementNo", item.getSettlementNo(),
                "paidAmount", item.getPaidAmount() != null ? item.getPaidAmount() : BigDecimal.ZERO
        );
    }

    private List<Map<String, Object>> defaultItems(Settlement item) {
        BigDecimal total = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = item.getPaidAmount() != null ? item.getPaidAmount() : BigDecimal.ZERO;
        return List.of(
                Map.of("name", "诊疗费", "amount", total),
                Map.of("name", "已付", "amount", paid)
        );
    }
}
