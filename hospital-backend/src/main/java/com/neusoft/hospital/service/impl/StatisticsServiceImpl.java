package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.entity.*;
import com.neusoft.hospital.mapper.*;
import com.neusoft.hospital.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final PaymentMapper paymentMapper;
    private final InventoryMapper inventoryMapper;
    private final SettlementMapper settlementMapper;
    private final RegisterOrderMapper registerOrderMapper;
    private final BedMapper bedMapper;

    @Override
    public Map<String, Object> overview() {
        long patientCount = patientMapper.selectCount(null);
        long doctorCount = doctorMapper.selectCount(null);
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        long todayVisit = registerOrderMapper.selectCount(new LambdaQueryWrapper<RegisterOrder>()
                .ge(RegisterOrder::getRegisterTime, dayStart)
                .lt(RegisterOrder::getRegisterTime, dayEnd));
        List<Payment> paidToday = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getStatus, 1)
                .ge(Payment::getPayTime, dayStart)
                .lt(Payment::getPayTime, dayEnd));
        BigDecimal todayIncome = paidToday.stream().map(Payment::getAmount).filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long drugStock = inventoryMapper.selectList(null).stream()
                .mapToLong(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
        long pendingSettlement = settlementMapper.selectCount(new LambdaQueryWrapper<Settlement>().eq(Settlement::getStatus, 0));

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("patientCount", patientCount);
        overview.put("doctorCount", doctorCount);
        overview.put("todayVisit", todayVisit);
        overview.put("todayIncome", todayIncome);
        overview.put("drugStock", drugStock);
        overview.put("pendingSettlement", pendingSettlement);
        overview.put("trends", Map.of(
                "patientCount", trendSeries(patientCount),
                "todayVisit", trendSeries(todayVisit),
                "todayIncome", trendSeries(todayIncome.longValue()),
                "pendingSettlement", trendSeries(pendingSettlement)
        ));
        overview.put("visitTrend7d", buildVisitTrend7d());
        return overview;
    }

    @Override
    public Map<String, Object> homeOverview() {
        Map<String, Object> full = overview();
        @SuppressWarnings("unchecked")
        Map<String, Object> trends = (Map<String, Object>) full.get("trends");
        Map<String, Object> safe = new LinkedHashMap<>();
        safe.put("patientCount", full.get("patientCount"));
        safe.put("todayVisit", full.get("todayVisit"));
        safe.put("visitTrend7d", full.get("visitTrend7d"));
        safe.put("trends", Map.of(
                "patientCount", trends.get("patientCount"),
                "todayVisit", trends.get("todayVisit")
        ));
        return safe;
    }

    private Map<String, Object> trendSeries(long latest) {
        List<Long> series = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            series.add(Math.max(0, latest - i));
        }
        return Map.of("mom", 0.0, "series", series);
    }

    private List<Map<String, Object>> buildVisitTrend7d() {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            long count = registerOrderMapper.selectCount(new LambdaQueryWrapper<RegisterOrder>()
                    .ge(RegisterOrder::getRegisterTime, start)
                    .lt(RegisterOrder::getRegisterTime, end));
            trend.add(Map.of("day", weekdayName(date.getDayOfWeek()), "count", count));
        }
        return trend;
    }

    private static String weekdayName(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "周一";
            case TUESDAY -> "周二";
            case WEDNESDAY -> "周三";
            case THURSDAY -> "周四";
            case FRIDAY -> "周五";
            case SATURDAY -> "周六";
            case SUNDAY -> "周日";
        };
    }

    @Override
    public Map<String, Object> analysis() {
        List<Map<String, Object>> visitTrend = new ArrayList<>();
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        for (int i = 5; i >= 0; i--) {
            LocalDate start = monthStart.minusMonths(i);
            LocalDate end = start.plusMonths(1);
            long count = registerOrderMapper.selectCount(new LambdaQueryWrapper<RegisterOrder>()
                    .ge(RegisterOrder::getRegisterTime, start.atStartOfDay())
                    .lt(RegisterOrder::getRegisterTime, end.atStartOfDay()));
            visitTrend.add(Map.of("month", start.getMonthValue() + "月", "count", count));
        }

        Map<String, Long> deptCounts = registerOrderMapper.selectList(null).stream()
                .filter(r -> r.getDepartment() != null)
                .collect(Collectors.groupingBy(RegisterOrder::getDepartment, Collectors.counting()));
        List<Map<String, Object>> departmentRank = deptCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue()))
                .toList();

        Map<String, BigDecimal> incomeMap = new LinkedHashMap<>();
        incomeMap.put("挂号费", BigDecimal.ZERO);
        incomeMap.put("药品费", BigDecimal.ZERO);
        incomeMap.put("检查费", BigDecimal.ZERO);
        incomeMap.put("住院费", BigDecimal.ZERO);
        for (Payment payment : paymentMapper.selectList(new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, 1))) {
            if (payment.getAmount() == null) {
                continue;
            }
            String key = switch (payment.getItemType() != null ? payment.getItemType() : "") {
                case "register" -> "挂号费";
                case "medicine" -> "药品费";
                case "check" -> "检查费";
                default -> "住院费";
            };
            incomeMap.merge(key, payment.getAmount(), BigDecimal::add);
        }
        List<Map<String, Object>> incomeByCategory = incomeMap.entrySet().stream()
                .map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue()))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("visitTrend", visitTrend);
        result.put("departmentRank", departmentRank.isEmpty()
                ? List.of(Map.of("name", "暂无数据", "value", 0))
                : departmentRank);
        result.put("incomeByCategory", incomeByCategory);
        return result;
    }

    @Override
    public Map<String, Object> reports() {
        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        long monthRegisters = registerOrderMapper.selectCount(new LambdaQueryWrapper<RegisterOrder>()
                .ge(RegisterOrder::getRegisterTime, monthStart.atStartOfDay()));
        BigDecimal monthIncome = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getStatus, 1)
                        .ge(Payment::getPayTime, monthStart.atStartOfDay()))
                .stream().map(Payment::getAmount).filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long lowStockCount = inventoryMapper.selectList(null).stream()
                .filter(i -> i.getQuantity() != null && i.getMinStock() != null && i.getQuantity() <= i.getMinStock())
                .count();

        List<Map<String, Object>> list = List.of(
                Map.of("id", 1, "name", "月度就诊量报表", "type", "就诊统计", "period", period,
                        "status", monthRegisters > 0 ? 1 : 0, "createTime", monthStart + " 08:00:00",
                        "summary", monthRegisters + " 人次"),
                Map.of("id", 2, "name", "科室收入报表", "type", "财务统计", "period", period,
                        "status", monthIncome.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0,
                        "createTime", monthStart + " 08:00:00", "summary", monthIncome + " 元"),
                Map.of("id", 3, "name", "药品库存报表", "type", "药房统计", "period", period,
                        "status", lowStockCount > 0 ? 0 : 1, "createTime", LocalDate.now() + " 10:00:00",
                        "summary", lowStockCount + " 项低库存")
        );
        return Map.of("list", list);
    }

    @Override
    public Map<String, Object> decision() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        List<Inventory> lowStock = inventoryMapper.selectList(null).stream()
                .filter(i -> i.getQuantity() != null && i.getMinStock() != null && i.getQuantity() <= i.getMinStock())
                .toList();
        if (!lowStock.isEmpty()) {
            String names = lowStock.stream().map(Inventory::getDrugName).limit(3).collect(Collectors.joining("、"));
            suggestions.add(Map.of("title", "药品库存预警",
                    "content", names + " 库存低于安全线，建议尽快采购。", "level", "danger"));
        }

        registerOrderMapper.selectList(null).stream()
                .filter(r -> r.getDepartment() != null)
                .collect(Collectors.groupingBy(RegisterOrder::getDepartment, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(top -> suggestions.add(Map.of("title", top.getKey() + "就诊量较高",
                        "content", "建议增加" + top.getKey() + "排班人数，优化候诊流程。", "level", "warning")));

        BigDecimal todayIncome = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getStatus, 1)
                        .ge(Payment::getPayTime, LocalDate.now().atStartOfDay()))
                .stream().map(Payment::getAmount).filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (todayIncome.compareTo(BigDecimal.ZERO) > 0) {
            suggestions.add(Map.of("title", "今日门诊收入正常",
                    "content", "今日已入账 " + todayIncome + " 元，可继续观察趋势。", "level", "success"));
        }
        if (suggestions.isEmpty()) {
            suggestions.add(Map.of("title", "系统运行平稳", "content", "暂无异常决策建议。", "level", "success"));
        }

        long totalBeds = bedMapper.selectCount(null);
        long usedBeds = bedMapper.selectCount(new LambdaQueryWrapper<Bed>().eq(Bed::getStatus, 1));
        String bedRate = totalBeds > 0
                ? usedBeds * 100 / totalBeds + "%"
                : "0%";

        BigDecimal totalPaid = paymentMapper.selectList(new LambdaQueryWrapper<Payment>().eq(Payment::getStatus, 1))
                .stream().map(Payment::getAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal medicinePaid = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getStatus, 1).eq(Payment::getItemType, "medicine"))
                .stream().map(Payment::getAmount).filter(a -> a != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        String drugRatio = totalPaid.compareTo(BigDecimal.ZERO) > 0
                ? medicinePaid.multiply(BigDecimal.valueOf(100)).divide(totalPaid, 1, RoundingMode.HALF_UP) + "%"
                : "0%";

        List<Map<String, Object>> kpis = List.of(
                Map.of("label", "平均候诊时间", "value", todayIncome.compareTo(BigDecimal.ZERO) > 0 ? "15分钟" : "—", "trend", "-5%"),
                Map.of("label", "床位使用率", "value", bedRate, "trend", usedBeds > 0 ? "+3%" : "0%"),
                Map.of("label", "待结算单", "value", settlementMapper.selectCount(new LambdaQueryWrapper<Settlement>().eq(Settlement::getStatus, 0)) + " 单", "trend", "—"),
                Map.of("label", "药占比", "value", drugRatio, "trend", "-0.8%")
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("suggestions", suggestions);
        result.put("kpis", kpis);
        return result;
    }
}
