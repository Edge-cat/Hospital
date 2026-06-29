package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;

import com.neusoft.hospital.common.PageQuery;

import com.neusoft.hospital.common.PageResult;

import com.neusoft.hospital.entity.IncomeExpense;

import com.neusoft.hospital.mapper.IncomeExpenseMapper;

import com.neusoft.hospital.service.IncomeExpenseService;

import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.SourceModuleLabels;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



@Service

@RequiredArgsConstructor

public class IncomeExpenseServiceImpl implements IncomeExpenseService {



    private final IncomeExpenseMapper mapper;



    @Override

    public PageResult<IncomeExpense> list(PageQuery query) {

        return EntityPageHelper.page(mapper, query, w ->

                EntityPageHelper.keywordLike(w, query.getKeyword(), IncomeExpense::getCategory));

    }



    @Override

    public IncomeExpense getById(Long id) {

        IncomeExpense entity = mapper.selectById(id);

        if (entity == null) {

            throw new BusinessException(404, "记录不存在");

        }

        return entity;

    }



    @Override

    public void create(IncomeExpense entity) {

        if (entity.getCreateTime() == null) {

            entity.setCreateTime(LocalDateTime.now());

        }

        if (entity.getRecordTime() == null) {

            entity.setRecordTime(LocalDateTime.now());

        }

        mapper.insert(entity);

    }



    @Override

    public void createFromMap(Map<String, Object> body) {

        IncomeExpense entity = new IncomeExpense();

        String type = body.get("type") != null ? body.get("type").toString() : "收入";

        entity.setRecordType("收入".equals(type) ? "income" : "支出".equals(type) ? "expense" : type);

        entity.setCategory(body.get("category") != null ? body.get("category").toString() : "");

        entity.setAmount(body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : BigDecimal.ZERO);

        entity.setRemark(body.get("remark") != null ? body.get("remark").toString() : "");

        entity.setOperator("系统管理员");

        entity.setRecordNo("IE" + System.currentTimeMillis());

        entity.setRecordTime(LocalDateTime.now());

        entity.setCreateTime(LocalDateTime.now());

        mapper.insert(entity);

    }



    @Override

    public Map<String, Object> summary(String startDate, String endDate) {
        List<IncomeExpense> all = mapper.selectList(null);
        LocalDate start = StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : null;
        LocalDate end = StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : null;
        BigDecimal income = sumByType(all, start, end, true);
        BigDecimal expense = sumByType(all, start, end, false);

        LocalDate prevStart = null;
        LocalDate prevEnd = null;
        if (start != null && end != null) {
            long days = end.toEpochDay() - start.toEpochDay() + 1;
            prevEnd = start.minusDays(1);
            prevStart = prevEnd.minusDays(days - 1);
        }
        BigDecimal prevIncome = sumByType(all, prevStart, prevEnd, true);
        BigDecimal prevExpense = sumByType(all, prevStart, prevEnd, false);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("income", income);
        result.put("expense", expense);
        result.put("balance", income.subtract(expense));
        result.put("incomeMom", momPercent(income, prevIncome));
        result.put("expenseMom", momPercent(expense, prevExpense));
        result.put("incomeTrend", buildDailyTrend(all, true));
        result.put("expenseTrend", buildDailyTrend(all, false));
        return result;
    }

    private BigDecimal sumByType(List<IncomeExpense> all, LocalDate start, LocalDate end, boolean incomeType) {
        BigDecimal total = BigDecimal.ZERO;
        for (IncomeExpense item : all) {
            if (item.getRecordTime() == null) {
                continue;
            }
            LocalDate d = item.getRecordTime().toLocalDate();
            if (start != null && d.isBefore(start)) {
                continue;
            }
            if (end != null && d.isAfter(end)) {
                continue;
            }
            boolean income = "income".equalsIgnoreCase(item.getRecordType()) || "收入".equals(item.getRecordType());
            if (incomeType != income) {
                continue;
            }
            total = total.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
        }
        return total;
    }

    private double momPercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<BigDecimal> buildDailyTrend(List<IncomeExpense> all, boolean incomeType) {
        List<BigDecimal> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            BigDecimal dayTotal = BigDecimal.ZERO;
            for (IncomeExpense item : all) {
                if (item.getRecordTime() == null) {
                    continue;
                }
                LocalDate d = item.getRecordTime().toLocalDate();
                if (!d.equals(day)) {
                    continue;
                }
                boolean income = "income".equalsIgnoreCase(item.getRecordType()) || "收入".equals(item.getRecordType());
                if (incomeType == income) {
                    dayTotal = dayTotal.add(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
                }
            }
            trend.add(dayTotal);
        }
        return trend;
    }



    @Override

    public Map<String, Object> trace(Long id) {

        IncomeExpense item = getById(id);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", item.getId());

        result.put("type", item.getType());

        result.put("category", item.getCategory());

        result.put("amount", item.getAmount());

        result.put("recordDate", item.getRecordDate());

        result.put("sourceModule", item.getSourceModuleLabel());

        result.put("sourceDoc", Map.of(

                "module", item.getSourceModuleLabel(),

                "docNo", item.getSourceId() != null ? item.getSourceId() : (item.getRecordNo() != null ? item.getRecordNo() : "-"),

                "path", SourceModuleLabels.path(item.getSourceModuleCode())

        ));

        return result;

    }



    @Override

    public void update(Long id, IncomeExpense entity) {

        getById(id);

        entity.setId(id);

        mapper.updateById(entity);

    }



    @Override

    public void delete(Long id) {

        mapper.deleteById(id);

    }

}

