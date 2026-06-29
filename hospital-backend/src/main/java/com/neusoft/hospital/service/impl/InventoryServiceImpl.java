package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Inventory;
import com.neusoft.hospital.entity.Procurement;
import com.neusoft.hospital.mapper.InventoryMapper;
import com.neusoft.hospital.mapper.ProcurementMapper;
import com.neusoft.hospital.service.InventoryService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper mapper;
    private final ProcurementMapper procurementMapper;

    @Override
    public PageResult<Inventory> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Inventory::getDrugName);
            if (query.getStatus() != null) {
                w.eq(Inventory::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Inventory getById(Long id) {
        Inventory entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "库存记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Inventory entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Inventory entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public void adjust(Long id, Map<String, Object> body) {
        Inventory inv = getById(id);
        String type = body.get("type") != null ? body.get("type").toString() : "";
        int current = inv.getQuantity() != null ? inv.getQuantity() : 0;
        switch (type) {
            case "stocktake" -> {
                if (body.get("quantity") != null) {
                    inv.setQuantity(Integer.parseInt(body.get("quantity").toString()));
                }
            }
            case "disposal" -> {
                int deduct = body.get("quantity") != null ? Integer.parseInt(body.get("quantity").toString()) : 0;
                inv.setQuantity(Math.max(0, current - deduct));
            }
            case "transfer" -> {
                if (body.get("targetWarehouse") != null) {
                    inv.setWarehouse(body.get("targetWarehouse").toString());
                }
                if (body.get("quantity") != null) {
                    inv.setQuantity(Integer.parseInt(body.get("quantity").toString()));
                }
            }
            default -> throw new BusinessException("不支持的调整类型");
        }
        inv.setUpdateTime(LocalDateTime.now());
        mapper.updateById(inv);
    }

    @Override
    @Transactional
    public Map<String, Object> createProcurementRequest(Long id, Map<String, Object> body) {
        Inventory inv = getById(id);
        int stock = inv.getQuantity() != null ? inv.getQuantity() : 0;
        int minStock = inv.getMinStock() != null ? inv.getMinStock() : 50;
        int qty;
        if (body != null && body.get("quantity") != null) {
            qty = Integer.parseInt(body.get("quantity").toString());
        } else {
            qty = Math.max(minStock * 2 - stock, 100);
        }
        Procurement procurement = new Procurement();
        procurement.setOrderNo("PO" + System.currentTimeMillis());
        procurement.setDrugName(inv.getDrugName());
        procurement.setQuantity(qty);
        procurement.setUnitPrice(BigDecimal.valueOf(10));
        procurement.setSupplier("国药控股");
        procurement.setPhase(0);
        procurement.setUrgent(stock <= minStock ? 1 : 0);
        procurement.setOrderTime(LocalDateTime.now());
        procurement.setLogisticsNo("");
        procurement.setReceiptNote("由库存告警自动生成");
        procurement.setCreateTime(LocalDateTime.now());
        procurementMapper.insert(procurement);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("procurementId", procurement.getId());
        return result;
    }
}
