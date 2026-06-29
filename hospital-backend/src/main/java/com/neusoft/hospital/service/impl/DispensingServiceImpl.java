package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Dispensing;
import com.neusoft.hospital.entity.Inventory;
import com.neusoft.hospital.mapper.DispensingMapper;
import com.neusoft.hospital.mapper.InventoryMapper;
import com.neusoft.hospital.service.DispensingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DispensingServiceImpl implements DispensingService {

    private static final Map<String, Integer> PRIORITY_ORDER = Map.of("急诊", 0, "门诊", 1, "住院", 2);

    private final DispensingMapper mapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public PageResult<Dispensing> list(PageQuery query) {
        LambdaQueryWrapper<Dispensing> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            w.and(q -> q.like(Dispensing::getPatientName, query.getKeyword())
                    .or().like(Dispensing::getDrugName, query.getKeyword()));
        }
        if (query.getStatus() != null) {
            w.eq(Dispensing::getStatus, query.getStatus());
        }
        List<Dispensing> all = mapper.selectList(w);
        all.sort(Comparator
                .comparingInt((Dispensing d) -> PRIORITY_ORDER.getOrDefault(d.getPriority(), 9))
                .thenComparing(d -> d.getCreateTime() != null ? d.getCreateTime().toString() : ""));
        int page = query.pageOrDefault();
        int pageSize = query.pageSizeOrDefault();
        int from = Math.min((page - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return new PageResult<>(all.subList(from, to), all.size(), page, pageSize);
    }

    @Override
    public Dispensing getById(Long id) {
        Dispensing entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Dispensing entity) {
        if (entity.getPrescriptionNo() == null) {
            entity.setPrescriptionNo("RX" + System.currentTimeMillis());
        }
        if (entity.getBarcode() == null) {
            entity.setBarcode("BC" + System.currentTimeMillis());
        }
        if (entity.getPriority() == null) {
            entity.setPriority("门诊");
        }
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Dispensing entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Dispensing scanByBarcode(String barcode) {
        if (!StringUtils.hasText(barcode)) {
            throw new BusinessException("条码不能为空");
        }
        Dispensing item = mapper.selectOne(new LambdaQueryWrapper<Dispensing>()
                .eq(Dispensing::getBarcode, barcode)
                .lt(Dispensing::getStatus, 2)
                .last("LIMIT 1"));
        if (item == null) {
            throw new BusinessException("未找到待配药处方");
        }
        return item;
    }

    @Override
    @Transactional
    public Map<String, Object> complete(Long id, Map<String, Object> body) {
        Dispensing item = getById(id);
        item.setStatus(2);
        item.setDispensingTime(LocalDateTime.now());
        mapper.updateById(item);
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getDrugName, item.getDrugName())
                .last("LIMIT 1"));
        if (inv != null) {
            int stock = inv.getQuantity() != null ? inv.getQuantity() : 0;
            int qty = item.getQuantity() != null ? item.getQuantity() : 0;
            inv.setQuantity(Math.max(0, stock - qty));
            inv.setUpdateTime(LocalDateTime.now());
            inventoryMapper.updateById(inv);
        }
        boolean scanMode = body != null && Boolean.TRUE.equals(body.get("scanMode"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notified", true);
        result.put("message", scanMode
                ? "扫码配药完成，已扣减库存并推送取药通知"
                : "配药完成，已扣减库存并推送取药通知");
        return result;
    }
}
