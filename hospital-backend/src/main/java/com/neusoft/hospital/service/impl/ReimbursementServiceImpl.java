package com.neusoft.hospital.service.impl;

import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.common.PageQuery;
import com.neusoft.hospital.common.PageResult;
import com.neusoft.hospital.entity.Reimbursement;
import com.neusoft.hospital.mapper.ReimbursementMapper;
import com.neusoft.hospital.service.ReimbursementService;
import com.neusoft.hospital.service.support.EntityPageHelper;
import com.neusoft.hospital.service.support.FinanceLedgerHelper;
import com.neusoft.hospital.service.support.JsonStoreHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReimbursementServiceImpl implements ReimbursementService {

    private final ReimbursementMapper mapper;
    private final JsonStoreHelper jsonStoreHelper;

    @Override
    public PageResult<Reimbursement> list(PageQuery query) {
        return EntityPageHelper.page(mapper, query, w -> {
            EntityPageHelper.keywordLike(w, query.getKeyword(), Reimbursement::getApplicant);
            if (query.getStatus() != null) {
                w.eq(Reimbursement::getStatus, query.getStatus());
            }
        });
    }

    @Override
    public Reimbursement getById(Long id) {
        Reimbursement entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "记录不存在");
        }
        return entity;
    }

    @Override
    public void create(Reimbursement entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getApplyTime() == null) {
            entity.setApplyTime(now);
        }
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(now);
        }
        if (!StringUtils.hasText(entity.getWorkflowJson())) {
            entity.setWorkflowJson(jsonStoreHelper.toJson(defaultWorkflow(entity)));
        }
        if (!StringUtils.hasText(entity.getAttachmentsJson())) {
            entity.setAttachmentsJson(jsonStoreHelper.toJson(List.of("发票扫描件.pdf")));
        }
        mapper.insert(entity);
    }

    @Override
    public void update(Long id, Reimbursement entity) {
        getById(id);
        entity.setId(id);
        mapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        Reimbursement item = getById(id);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("id", item.getId());
        detail.put("applyNo", item.getApplyNo());
        detail.put("applicant", item.getApplicant());
        detail.put("department", item.getDepartment());
        detail.put("amount", item.getAmount());
        detail.put("reason", item.getReason());
        detail.put("status", item.getStatus());
        detail.put("applyDate", item.getApplyTime() != null ? item.getApplyTime().toLocalDate().toString() : null);
        List<Map<String, Object>> workflow = jsonStoreHelper.readList(item.getWorkflowJson());
        detail.put("workflow", workflow.isEmpty() ? defaultWorkflow(item) : workflow);
        List<String> attachments = jsonStoreHelper.readStringList(item.getAttachmentsJson());
        detail.put("attachments", attachments.isEmpty() ? List.of("发票扫描件.pdf") : attachments);
        return detail;
    }

    @Override
    public void approve(Long id, Map<String, Object> body) {
        Reimbursement item = getById(id);
        item.setStatus(1);
        item.setApproveTime(LocalDateTime.now());
        item.setApproveOperator(FinanceLedgerHelper.currentOperator());
        List<Map<String, Object>> workflow = new ArrayList<>(jsonStoreHelper.readList(item.getWorkflowJson()));
        if (workflow.isEmpty()) {
            workflow = new ArrayList<>(defaultWorkflow(item));
        }
        for (Map<String, Object> node : workflow) {
            if ("科室审批".equals(node.get("node"))) {
                node.put("status", "done");
                node.put("time", item.getApproveTime().toString().replace('T', ' '));
            }
            if ("财务打款".equals(node.get("node"))) {
                node.put("status", "done");
                node.put("time", item.getApproveTime().toString().replace('T', ' '));
            }
        }
        workflow.add(Map.of(
                "node", "审批通过",
                "time", item.getApproveTime().toString().replace('T', ' '),
                "operator", item.getApproveOperator(),
                "remark", body != null && body.get("remark") != null ? body.get("remark").toString() : ""
        ));
        item.setWorkflowJson(jsonStoreHelper.toJson(workflow));
        mapper.updateById(item);
    }

    @Override
    public void reject(Long id, Map<String, Object> body) {
        Reimbursement item = getById(id);
        item.setStatus(2);
        List<Map<String, Object>> workflow = new ArrayList<>(jsonStoreHelper.readList(item.getWorkflowJson()));
        if (workflow.isEmpty()) {
            workflow = new ArrayList<>(defaultWorkflow(item));
        }
        workflow.add(Map.of(
                "node", "审批驳回",
                "time", LocalDateTime.now().toString().replace('T', ' '),
                "operator", FinanceLedgerHelper.currentOperator(),
                "remark", body != null && body.get("remark") != null ? body.get("remark").toString() : ""
        ));
        item.setWorkflowJson(jsonStoreHelper.toJson(workflow));
        mapper.updateById(item);
    }

    private List<Map<String, Object>> defaultWorkflow(Reimbursement item) {
        return List.of(
                Map.of("node", "提交申请", "time", item.getApplyTime(), "operator", item.getApplicant(), "status", "done"),
                Map.of("node", "科室审批", "time", item.getApplyTime(), "operator", "科室主任",
                        "status", item.getStatus() != null && item.getStatus() >= 1 ? "done" : "pending"),
                Map.of("node", "财务打款", "time", "", "operator", "财务部",
                        "status", item.getStatus() != null && item.getStatus() == 1 ? "done" : "pending")
        );
    }
}
