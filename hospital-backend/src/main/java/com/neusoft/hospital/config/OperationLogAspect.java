package com.neusoft.hospital.config;

import com.neusoft.hospital.common.OperationLog;
import com.neusoft.hospital.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog opLog) throws Throwable {
        int status = 1;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            status = 0;
            throw ex;
        } finally {
            operationLogService.recordBackend(opLog.module(), opLog.action(), status);
        }
    }
}
