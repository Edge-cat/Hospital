package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.FinanceAccount;
import com.neusoft.hospital.entity.IncomeExpense;
import com.neusoft.hospital.mapper.FinanceAccountMapper;
import com.neusoft.hospital.mapper.IncomeExpenseMapper;
import com.neusoft.hospital.service.FinanceService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.SourceModuleLabels;
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
public class FinanceServiceImpl implements FinanceService {

    private final FinanceAccountMapper mapper;
    private final IncomeExpenseMapper incomeExpenseMapper;

    @Override
    public PageResult<FinanceAccount> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), FinanceAccount::getAccountName);
            w.and(q -> q.isNull(FinanceAccount::getArchived).or().eq(FinanceAccount::getArchived, 0));
            if (query.getStatus() != null) {
                w.eq(FinanceAccount::getStatus, query.getStatus());
            }
            if (org.springframework.util.StringUtils.hasText(query.getBank())) {
                w.eq(FinanceAccount::getBank, query.getBank());
            }
        });
    }

    @Override
    public FinanceAccount getById(Long id) {
        FinanceAccount entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(FinanceAccount entity) {
        if (entity.getAccountNo() == null) {
            entity.setAccountNo("AC" + System.currentTimeMillis());
        }
        if (entity.getArchived() == null) {
            entity.setArchived(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        if (entity.getUpdateTime() == null) {
            entity.setUpdateTime(LocalDateTime.now());
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, FinanceAccount entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getFlows(Long id) {
        FinanceAccount account = getById(id);
        List<IncomeExpense> records = incomeExpenseMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IncomeExpense>()
                        .eq(IncomeExpense::getAccountId, id)
                        .orderByDesc(IncomeExpense::getRecordTime)
                        .last("LIMIT 50"));
        if (records.isEmpty()) {
            records = incomeExpenseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IncomeExpense>()
                            .orderByDesc(IncomeExpense::getId)
                            .last("LIMIT 12"));
        }
        List<Map<String, Object>> flows = new ArrayList<>();
        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        for (int i = 0; i < records.size(); i++) {
            IncomeExpense ie = records.get(i);
            Map<String, Object> flow = new LinkedHashMap<>();
            flow.put("id", i + 1);
            flow.put("time", ie.getRecordTime() != null ? ie.getRecordTime().toString().replace('T', ' ') : ie.getRecordDate() + " 00:00:00");
            flow.put("type", ie.getType());
            boolean income = "收入".equals(ie.getType());
            flow.put("amount", income ? ie.getAmount() : ie.getAmount().negate());
            balance = income ? balance.add(ie.getAmount()) : balance.subtract(ie.getAmount());
            flow.put("balance", balance);
            flow.put("remark", ie.getCategory() + " · " + SourceModuleLabels.label(ie.getSourceModuleCode()));
            flows.add(flow);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", flows);
        result.put("accountName", account.getAccountName());
        return result;
    }

    @Override
    public void freeze(Long id) {
        FinanceAccount account = getById(id);
        account.setStatus(0);
        mapper.updateById(account);
    }

    @Override
    public void archive(Long id) {
        FinanceAccount account = getById(id);
        account.setArchived(1);
        account.setStatus(0);
        mapper.updateById(account);
    }
}
