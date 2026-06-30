package com.neusoft.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neusoft.hospital.config.UserContext;
import com.neusoft.hospital.dto.OperationReportRequest;
import com.neusoft.hospital.entity.SysOperationLog;
import com.neusoft.hospital.mapper.SysOperationLogMapper;
import com.neusoft.hospital.service.OperationLogService;
import com.neusoft.hospital.service.support.FinanceLedgerHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final SysOperationLogMapper operationLogMapper;

    @Override
    public void recordBackend(String module, String action, int status) {
        insert("backend", null, module, action, null, null, status, FinanceLedgerHelper.currentOperator(), resolveIp());
    }

    @Override
    public void recordFrontend(OperationReportRequest request) {
        if (request == null || !StringUtils.hasText(request.getModule()) || !StringUtils.hasText(request.getAction())) {
            return;
        }
        String operator = currentOperatorName();
        int status = request.getStatus() != null ? request.getStatus() : 1;
        insert("frontend", request.getClient(), request.getModule(), request.getAction(),
                request.getPath(), request.getDetail(), status, operator, resolveIp());
    }

    @Override
    public void recordPatientAction(String client, String module, String action, String path, String detail) {
        OperationReportRequest request = new OperationReportRequest();
        request.setClient(resolveAuditClient(client));
        request.setModule(module);
        request.setAction(action);
        request.setPath(path);
        request.setDetail(detail);
        request.setStatus(1);
        recordFrontend(request);
    }

    @Override
    public List<SysOperationLog> listSince(Long sinceId, int limit) {
        long cursor = sinceId != null ? sinceId : 0L;
        int size = limit > 0 ? Math.min(limit, 50) : 20;
        return operationLogMapper.selectList(new LambdaQueryWrapper<SysOperationLog>()
                .gt(SysOperationLog::getId, cursor)
                .orderByAsc(SysOperationLog::getId)
                .last("LIMIT " + size));
    }

    @Override
    public Map<String, Object> consoleOverview() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long todayTotal = operationLogMapper.selectCount(new LambdaQueryWrapper<SysOperationLog>()
                .ge(SysOperationLog::getCreateTime, start)
                .lt(SysOperationLog::getCreateTime, end));
        long frontendCount = operationLogMapper.selectCount(new LambdaQueryWrapper<SysOperationLog>()
                .ge(SysOperationLog::getCreateTime, start)
                .lt(SysOperationLog::getCreateTime, end)
                .eq(SysOperationLog::getSource, "frontend"));
        long backendCount = operationLogMapper.selectCount(new LambdaQueryWrapper<SysOperationLog>()
                .ge(SysOperationLog::getCreateTime, start)
                .lt(SysOperationLog::getCreateTime, end)
                .eq(SysOperationLog::getSource, "backend"));

        List<SysOperationLog> recent = operationLogMapper.selectList(new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getId)
                .last("LIMIT 20"));

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("todayTotal", todayTotal);
        overview.put("frontendCount", frontendCount);
        overview.put("backendCount", backendCount);
        overview.put("recent", recent);
        return overview;
    }

    private void insert(String source, String client, String module, String action,
                        String path, String detail, int status, String operator, String ip) {
        SysOperationLog log = new SysOperationLog();
        log.setSource(source);
        log.setClient(client);
        log.setModule(module);
        log.setAction(action);
        log.setPath(path);
        log.setDetail(detail);
        log.setStatus(status);
        log.setOperator(StringUtils.hasText(operator) ? operator : "system");
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    private String currentOperatorName() {
        UserContext.LoginUser user = UserContext.get();
        if (user != null && StringUtils.hasText(user.getName())) {
            return user.getName();
        }
        if (user != null && StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return FinanceLedgerHelper.currentOperator();
    }

    private String resolveIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "127.0.0.1";
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveAuditClient(String fallback) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String header = attrs.getRequest().getHeader("X-Audit-Client");
            if (StringUtils.hasText(header)) {
                return header;
            }
        }
        return StringUtils.hasText(fallback) ? fallback : "mini";
    }
}
