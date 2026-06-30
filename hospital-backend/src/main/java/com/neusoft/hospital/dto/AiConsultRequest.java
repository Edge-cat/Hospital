package com.neusoft.hospital.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AiConsultRequest {

    @NotNull(message = "缺少就诊记录 ID")
    private Long recordId;

    @NotEmpty(message = "对话消息不能为空")
    @Size(max = 20, message = "单次对话轮次过多，请开启新会话")
    @Valid
    private List<AiMessageDto> messages;
}
