package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Drug;
import com.neusoft.hospital.entity.Inventory;
import com.neusoft.hospital.entity.Procurement;
import com.neusoft.hospital.mapper.DrugMapper;
import com.neusoft.hospital.mapper.InventoryMapper;
import com.neusoft.hospital.mapper.ProcurementMapper;
import com.neusoft.hospital.service.DrugService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements DrugService {

    private final DrugMapper mapper;
    private final InventoryMapper inventoryMapper;
    private final ProcurementMapper procurementMapper;

    @Override
    public PageResult<Drug> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Drug::getDrugName);
            w.and(q -> q.isNull(Drug::getArchived).or().eq(Drug::getArchived, 0));
            if (query.getStatus() != null) {
                w.eq(Drug::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Drug getById(Long id) {
        Drug entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Drug entity) {
        if (entity.getDrugCode() == null) {
            entity.setDrugCode("DR" + String.valueOf(System.currentTimeMillis()).substring(7));
        }
        if (entity.getArchived() == null) {
            entity.setArchived(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getDrugType() == null) {
            entity.setDrugType("处方药");
        }
        if (entity.getRiskLevel() == null) {
            entity.setRiskLevel("普通");
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Drug entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getInventory(Long id) {
        Drug drug = getById(id);
        List<Inventory> inv = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getDrugId, id)
                .or()
                .eq(Inventory::getDrugName, drug.getDrugName()));
        int totalStock = inv.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", inv);
        result.put("totalStock", totalStock);
        return result;
    }

    @Override
    public Map<String, Object> getProcurementTrend(Long id) {
        Drug drug = getById(id);
        List<Procurement> orders = procurementMapper.selectList(new LambdaQueryWrapper<Procurement>()
                .eq(Procurement::getDrugName, drug.getDrugName())
                .orderByDesc(Procurement::getOrderTime)
                .last("LIMIT 6"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Procurement p : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", p.getOrderDate() != null && p.getOrderDate().length() >= 7
                    ? p.getOrderDate().substring(0, 7) : p.getOrderDate());
            item.put("quantity", p.getQuantity());
            BigDecimal amount = p.getUnitPrice() != null && p.getQuantity() != null
                    ? p.getUnitPrice().multiply(BigDecimal.valueOf(p.getQuantity()))
                    : BigDecimal.ZERO;
            item.put("amount", amount);
            list.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        return result;
    }

    @Override
    public void archive(Long id) {
        Drug drug = getById(id);
        drug.setArchived(1);
        drug.setStatus(0);
        mapper.updateById(drug);
    }
}
