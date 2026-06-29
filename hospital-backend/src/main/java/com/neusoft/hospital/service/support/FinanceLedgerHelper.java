package com.neusoft.hospital.service.support;

import com.neusoft.hospital.config.UserContext;
import com.neusoft.hospital.entity.FinanceAccount;
import com.neusoft.hospital.entity.IncomeExpense;
import com.neusoft.hospital.mapper.FinanceAccountMapper;
import com.neusoft.hospital.mapper.IncomeExpenseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FinanceLedgerHelper {

    public static final long OUTPATIENT_ACCOUNT_ID = 1L;
    public static final long INPATIENT_ACCOUNT_ID = 3L;

    private final IncomeExpenseMapper incomeExpenseMapper;
    private final FinanceAccountMapper financeAccountMapper;

    public void recordIncome(Long accountId, BigDecimal amount, String category, String sourceModule,
                             String sourceId, String department, String operator) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        FinanceAccount account = financeAccountMapper.selectById(accountId);
        if (account == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        IncomeExpense record = new IncomeExpense();
        record.setRecordNo("IE" + System.currentTimeMillis());
        record.setRecordType("income");
        record.setCategory(category);
        record.setAmount(amount);
        record.setDepartment(department);
        record.setOperator(operator != null ? operator : currentOperator());
        record.setAccountId(accountId);
        record.setSourceModule(sourceModule);
        record.setSourceId(sourceId);
        record.setRecordTime(now);
        record.setCreateTime(now);
        incomeExpenseMapper.insert(record);

        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        account.setBalance(balance.add(amount));
        account.setUpdateTime(now);
        financeAccountMapper.updateById(account);
    }

    public void recordRefund(Long accountId, BigDecimal amount, String category, String sourceModule,
                             String sourceId, String department, String operator) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        FinanceAccount account = financeAccountMapper.selectById(accountId);
        if (account == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        IncomeExpense record = new IncomeExpense();
        record.setRecordNo("RF" + System.currentTimeMillis());
        record.setRecordType("expense");
        record.setCategory(category != null ? category : "退款");
        record.setAmount(amount);
        record.setDepartment(department);
        record.setOperator(operator != null ? operator : currentOperator());
        record.setAccountId(accountId);
        record.setSourceModule(sourceModule);
        record.setSourceId(sourceId);
        record.setRemark("退款冲减");
        record.setRecordTime(now);
        record.setCreateTime(now);
        incomeExpenseMapper.insert(record);

        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        account.setBalance(balance.subtract(amount));
        account.setUpdateTime(now);
        financeAccountMapper.updateById(account);
    }

    public static long resolveAccountId(String itemType) {
        return OUTPATIENT_ACCOUNT_ID;
    }

    public static String currentOperator() {
        UserContext.LoginUser user = UserContext.get();
        return user != null && user.getName() != null ? user.getName() : "系统";
    }
}
