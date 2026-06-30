package com.neusoft.hospital.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.neusoft.hospital.common.BusinessException;
import com.neusoft.hospital.config.AiProperties;
import com.neusoft.hospital.config.UserContext;
import com.neusoft.hospital.dto.AiConsultRequest;
import com.neusoft.hospital.dto.AiConsultResponse;
import com.neusoft.hospital.dto.AiDoctorAssistRequest;
import com.neusoft.hospital.dto.AiDoctorContextDto;
import com.neusoft.hospital.dto.AiDoctorDrugDto;
import com.neusoft.hospital.dto.AiMessageDto;
import com.neusoft.hospital.entity.MedicalRecord;
import com.neusoft.hospital.service.AiConsultService;
import com.neusoft.hospital.service.MedicalRecordService;
import com.neusoft.hospital.service.support.PatientScopeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConsultServiceImpl implements AiConsultService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String DISCLAIMER =
            "AI 回答仅供健康参考，不能替代医生诊断与处方。如有不适加重或异常反应，请立即回院或急诊就医。";

    private static final String DOCTOR_DISCLAIMER =
            "AI 分析仅供临床参考，不能替代接诊医生的独立判断与正式医嘱。请结合查体、检验及机构规范决策。";

    private static final Set<String> STAFF_ROLES = Set.of("admin", "doctor", "nurse");

    private static final Pattern CHIEF = Pattern.compile("主诉[：:]\\s*([\\s\\S]*?)(?=\\n\\s*(?:检查项目|处方)[：:]|$)");
    private static final Pattern EXAM = Pattern.compile("检查项目[：:]\\s*([^\\n]+)");
    private static final Pattern RX = Pattern.compile("处方[：:]\\s*\\n?([\\s\\S]*)");

    private final AiProperties aiProperties;
    private final MedicalRecordService medicalRecordService;
    private final PatientScopeHelper patientScopeHelper;
    private final RestTemplate restTemplate;

    @Override
    public AiConsultResponse consult(AiConsultRequest request) {
        MedicalRecord record = medicalRecordService.getById(request.getRecordId());
        patientScopeHelper.assertOwnsPatient(record.getPatientId());
        if (record.getStatus() == null || record.getStatus() != 2) {
            throw new BusinessException(409, "该就诊记录尚未解锁，请先完成相关缴费");
        }

        String systemPrompt = buildSystemPrompt(record);
        String lastUser = findLastUserMessage(request.getMessages());
        if (!StringUtils.hasText(lastUser)) {
            throw new BusinessException(400, "请输入您的问题");
        }

        if (!aiProperties.isConfigured()) {
            return new AiConsultResponse(buildDemoReply(record, lastUser), DISCLAIMER, true);
        }

        try {
            String reply = callDeepSeek(systemPrompt, request.getMessages());
            return new AiConsultResponse(reply, DISCLAIMER, false);
        } catch (BusinessException ex) {
            throw ex;
        } catch (HttpStatusCodeException ex) {
            String body = ex.getResponseBodyAsString();
            log.warn("DeepSeek API HTTP {}: {}", ex.getStatusCode(), body);
            if (body != null && body.contains("Insufficient Balance")) {
                throw new BusinessException(402, "DeepSeek 账户余额不足，请充值后重试");
            }
            throw new BusinessException(502, "AI 服务暂时不可用，请稍后重试");
        } catch (RestClientException ex) {
            log.warn("DeepSeek API 调用失败: {}", ex.getMessage());
            throw new BusinessException(502, "AI 服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public AiConsultResponse doctorAssist(AiDoctorAssistRequest request) {
        assertStaffRole();
        AiDoctorContextDto ctx = request.getContext();
        if (ctx == null) {
            throw new BusinessException(400, "缺少病历上下文");
        }

        String systemPrompt = buildDoctorSystemPrompt(ctx);
        String lastUser = findLastUserMessage(request.getMessages());
        if (!StringUtils.hasText(lastUser)) {
            throw new BusinessException(400, "请输入您的问题");
        }

        if (!aiProperties.isConfigured()) {
            return new AiConsultResponse(buildDoctorDemoReply(ctx, lastUser), DOCTOR_DISCLAIMER, true);
        }

        try {
            String reply = callDeepSeek(systemPrompt, request.getMessages());
            return new AiConsultResponse(reply, DOCTOR_DISCLAIMER, false);
        } catch (BusinessException ex) {
            throw ex;
        } catch (HttpStatusCodeException ex) {
            String body = ex.getResponseBodyAsString();
            log.warn("DeepSeek API HTTP {}: {}", ex.getStatusCode(), body);
            if (body != null && body.contains("Insufficient Balance")) {
                throw new BusinessException(402, "DeepSeek 账户余额不足，请充值后重试");
            }
            throw new BusinessException(502, "AI 服务暂时不可用，请稍后重试");
        } catch (RestClientException ex) {
            log.warn("DeepSeek API 调用失败: {}", ex.getMessage());
            throw new BusinessException(502, "AI 服务暂时不可用，请稍后重试");
        }
    }

    private void assertStaffRole() {
        UserContext.LoginUser user = UserContext.get();
        if (user == null || !StringUtils.hasText(user.getRole())) {
            throw new BusinessException(401, "未登录");
        }
        if (!STAFF_ROLES.contains(user.getRole())) {
            throw new BusinessException(403, "无权限使用 AI 临床辅助");
        }
    }

    private String buildDoctorSystemPrompt(AiDoctorContextDto ctx) {
        String gender = ctx.getGender() != null && ctx.getGender() == 1 ? "男"
                : ctx.getGender() != null && ctx.getGender() == 2 ? "女" : "—";
        String exams = formatExamList(ctx);
        String drugs = formatDrugList(ctx);

        return """
                你是东软云医院「临床决策支持」AI 助手，服务对象为执业医生。请基于下方正在书写中的门诊病历草稿，提供诊疗思路辅助。

                输出要求（简体中文、结构化分点）：
                1. 鉴别诊断思路：列出 2～4 个需鉴别的方向及支持/排除要点
                2. 建议补充的病史或查体：信息不足时优先说明
                3. 建议辅助检查：在已有勾选基础上，说明是否充分、可补充哪些
                4. 初步治疗/处置方向：原则性建议，不替代正式医嘱
                5. 危险信号：需警惕并及时处理的情况

                约束：
                - 不得编造未提供的检查结果或既往史
                - 不得直接下达正式处方/医嘱，仅作参考
                - 信息明显不足时，先列出需向患者追问的关键问题
                - 末尾提醒：最终决策由接诊医生负责

                【患者信息】
                姓名：%s
                编号：%s
                性别/年龄：%s / %s岁
                科室：%s

                【当前病历草稿】
                主诉：%s
                现病史：%s
                初步/确定诊断：%s
                拟开检查：%s
                检查备注：%s
                拟开药品：%s
                """.formatted(
                nullToDash(ctx.getPatientName()),
                nullToDash(ctx.getPatientNo()),
                gender,
                ctx.getAge() != null ? ctx.getAge() : "—",
                nullToDash(ctx.getDepartment()),
                nullToDash(ctx.getChiefComplaint()),
                nullToDash(ctx.getPresentIllness()),
                nullToDash(ctx.getDiagnosis()),
                exams,
                nullToDash(ctx.getExamNote()),
                drugs
        );
    }

    private String buildDoctorDemoReply(AiDoctorContextDto ctx, String question) {
        String diagnosis = nullToDash(ctx.getDiagnosis());
        String chief = nullToDash(ctx.getChiefComplaint());
        return """
                【演示模式】后端未配置 DEEPSEEK_API_KEY，以下为示例临床辅助：

                1. 鉴别诊断：结合主诉「%s」，需排除常见及需紧急处理的情况。
                2. 建议补充：完善相关系统查体，必要时完善血常规、影像等（按实际勾选项目执行）。
                3. 治疗方向：对症支持为主，待检查结果回报后调整；注意监测生命体征。
                4. 危险信号：症状进行性加重、意识改变、呼吸困难等需立即升级处理。

                您刚才问：「%s」
                配置 DeepSeek API Key 后可获得更详细的 AI 分析。
                """.formatted(chief, question.trim());
    }

    private String formatExamList(AiDoctorContextDto ctx) {
        if (ctx.getExams() == null || ctx.getExams().isEmpty()) {
            return "—";
        }
        return ctx.getExams().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining("、"));
    }

    private String formatDrugList(AiDoctorContextDto ctx) {
        if (ctx.getDrugs() == null || ctx.getDrugs().isEmpty()) {
            return "—";
        }
        return ctx.getDrugs().stream()
                .filter(d -> d != null && StringUtils.hasText(d.getName()))
                .map(d -> {
                    String line = d.getName().trim();
                    if (d.getQty() != null && d.getQty() > 0) {
                        line += " ×" + d.getQty();
                    }
                    if (StringUtils.hasText(d.getUsage())) {
                        line += "（" + d.getUsage().trim() + "）";
                    }
                    return line;
                })
                .collect(Collectors.joining("；"));
    }

    private String callDeepSeek(String systemPrompt, List<AiMessageDto> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        for (AiMessageDto msg : history) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent().trim()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", messages);
        body.put("max_tokens", aiProperties.getMaxTokens());
        body.put("temperature", aiProperties.getTemperature());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey().trim());

        String url = aiProperties.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

        JsonNode root = response.getBody();
        if (root == null) {
            throw new BusinessException(502, "AI 返回为空");
        }
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || !StringUtils.hasText(content.asText())) {
            throw new BusinessException(502, "AI 未返回有效内容");
        }
        return content.asText().trim();
    }

    private String buildSystemPrompt(MedicalRecord record) {
        ParsedTreatment parsed = parseTreatment(record.getTreatment());
        String visitTime = record.getVisitTime() != null ? record.getVisitTime().format(DT_FMT) : "—";

        return """
                你是东软云医院的 AI 健康助手。用户已完成就诊并缴费，请基于下方诊疗记录，用简体中文提供就诊后的康复建议、生活注意事项与复查规划。

                要求：
                1. 语气专业、温和、易懂，分点说明
                2. 不得修改、否定或替换医生已开具的医嘱与处方
                3. 涉及具体用药剂量调整时，提醒遵医嘱，不可自行停药或加量
                4. 回答末尾简要提醒：仅供参考，不能替代面诊

                【本次诊疗记录】
                记录编号：MR%s
                就诊科室：%s
                主治医生：%s
                就诊时间：%s
                主诉：%s
                诊断结果：%s
                检查项目：%s
                处方/医嘱：%s
                """.formatted(
                String.format("%08d", record.getId()),
                nullToDash(record.getDepartment()),
                nullToDash(record.getDoctorName()),
                visitTime,
                nullToDash(parsed.chiefComplaint()),
                nullToDash(record.getDiagnosis()),
                nullToDash(parsed.examItems()),
                nullToDash(parsed.prescription())
        );
    }

    private String buildDemoReply(MedicalRecord record, String question) {
        String dept = nullToDash(record.getDepartment());
        String diagnosis = nullToDash(record.getDiagnosis());
        return """
                【演示模式】后端未配置 DEEPSEEK_API_KEY，以下为基于本次记录的示例建议：

                1. 遵医嘱完成「%s」相关后续治疗，按时复查。
                2. 针对「%s」，注意休息与饮食清淡，避免劳累和烟酒刺激。
                3. 若出现发热不退、疼痛加重、皮疹或呼吸困难等，请立即回院或急诊。
                4. 用药期间留意说明书禁忌，勿自行增减剂量。

                您刚才问：「%s」
                配置 DeepSeek API Key 后，可获得更个性化的 AI 回复。
                """.formatted(dept, diagnosis, question.trim());
    }

    private String findLastUserMessage(List<AiMessageDto> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiMessageDto msg = messages.get(i);
            if ("user".equals(msg.getRole()) && StringUtils.hasText(msg.getContent())) {
                return msg.getContent().trim();
            }
        }
        return null;
    }

    private ParsedTreatment parseTreatment(String treatment) {
        String text = treatment != null ? treatment.trim() : "";
        if (!StringUtils.hasText(text)) {
            return new ParsedTreatment("", "", "");
        }
        Matcher chiefMatch = CHIEF.matcher(text);
        Matcher examMatch = EXAM.matcher(text);
        Matcher rxMatch = RX.matcher(text);
        String chief = chiefMatch.find() ? chiefMatch.group(1).trim() : "";
        String exam = examMatch.find() ? examMatch.group(1).trim() : "";
        String rx = rxMatch.find() ? rxMatch.group(1).trim() : "";
        if (!StringUtils.hasText(chief) && !StringUtils.hasText(exam) && !StringUtils.hasText(rx)) {
            return new ParsedTreatment(text, "", "");
        }
        return new ParsedTreatment(chief, exam, rx);
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value.trim() : "—";
    }

    private record ParsedTreatment(String chiefComplaint, String examItems, String prescription) {}
}
