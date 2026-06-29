package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Inventory;
import com.neusoft.hospital.entity.Procurement;
import com.neusoft.hospital.mapper.InventoryMapper;
import com.neusoft.hospital.mapper.ProcurementMapper;
import com.neusoft.hospital.service.ProcurementService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProcurementServiceImpl implements ProcurementService {

    private final ProcurementMapper mapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public PageResult<Procurement> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Procurement::getDrugName);
            if (query.getPhase() != null) {
                w.eq(Procurement::getPhase, query.getPhase());
            }
            if (StringUtils.hasText(query.getDrugName())) {
                w.eq(Procurement::getDrugName, query.getDrugName());
            }
            if (query.getStatus() != null) {
                w.eq(Procurement::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Procurement getById(Long id) {
        Procurement entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Procurement entity) {
        if (entity.getOrderNo() == null) {
            entity.setOrderNo("PO" + System.currentTimeMillis());
        }
        if (entity.getPhase() == null) {
            entity.setPhase(0);
        }
        if (entity.getUrgent() == null) {
            entity.setUrgent(0);
        }
        if (entity.getLogisticsNo() == null) {
            entity.setLogisticsNo("");
        }
        if (entity.getReceiptNote() == null) {
            entity.setReceiptNote("");
        }
        if (entity.getOrderTime() == null) {
            entity.setOrderTime(LocalDateTime.now());
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Procurement entity) {
        Procurement existing = getById(id);
        if (existing.getPhase() != null && existing.getPhase() == 3) {
            throw new BusinessException("已完成订单不可编辑");
        }
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getLogistics(Long id) {
        Procurement item = getById(id);
        int phase = item.getPhase() != null ? item.getPhase() : 0;
        LocalDate base = parseOrderDate(item.getOrderDate());

        List<Map<String, Object>> timeline = new ArrayList<>();
        timeline.add(event(base, "采购下单", true));
        timeline.add(event(base.plusDays(1), "供应商发货", phase >= 1));
        timeline.add(event(base.plusDays(2), "物流在途", phase >= 2));
        timeline.add(event(base.plusDays(3), "已入库", phase >= 3));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("logisticsNo", item.getLogisticsNo());
        result.put("receiptNote", item.getReceiptNote());
        result.put("supplier", item.getSupplier());
        result.put("phase", phase);
        result.put("timeline", timeline);
        return result;
    }

    @Override
    public void advance(Long id) {
        Procurement item = getById(id);
        if (item.getPhase() != null && item.getPhase() < 3) {
            item.setPhase(item.getPhase() + 1);
            mapper.updateById(item);
        }
    }

    @Override
    @Transactional
    public void stockIn(Long id) {
        Procurement item = getById(id);
        item.setPhase(3);
        mapper.updateById(item);
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getDrugName, item.getDrugName())
                .last("LIMIT 1"));
        if (inv != null) {
            inv.setQuantity((inv.getQuantity() != null ? inv.getQuantity() : 0) + (item.getQuantity() != null ? item.getQuantity() : 0));
            inv.setUpdateTime(LocalDateTime.now());
            inventoryMapper.updateById(inv);
        } else {
            Inventory newInv = new Inventory();
            newInv.setDrugName(item.getDrugName());
            newInv.setDrugCode("DR" + String.valueOf(System.currentTimeMillis()).substring(7));
            newInv.setQuantity(item.getQuantity());
            newInv.setMinStock(50);
            newInv.setBatchNo("BN" + System.currentTimeMillis());
            newInv.setExpiryDate(LocalDate.now().plusYears(1));
            newInv.setWarehouse("中心药房");
            newInv.setStatus(1);
            newInv.setUpdateTime(LocalDateTime.now());
            newInv.setCreateTime(LocalDateTime.now());
            inventoryMapper.insert(newInv);
        }
    }

    private Map<String, Object> event(LocalDate date, String label, boolean done) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("time", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        e.put("event", label);
        e.put("done", done);
        return e;
    }

    private LocalDate parseOrderDate(String orderDate) {
        if (orderDate == null || orderDate.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(orderDate.substring(0, Math.min(10, orderDate.length())));
        } catch (DateTimeParseException ex) {
            return LocalDate.now();
        }
    }
}
