package com.neusoft.hospital.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AiDoctorAssistRequest {

    private Long patientId;

    @NotNull(message = "缺少病历上下文")
    @Valid
    private AiDoctorContextDto context;

    @NotEmpty(message = "对话消息不能为空")
    @Size(max = 20, message = "单次对话轮次过多")
    @Valid
    private List<AiMessageDto> messages;
}
