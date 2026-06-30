package com.neusoft.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiMessageDto {

    @NotBlank(message = "消息角色不能为空")
    @Pattern(regexp = "user|assistant", message = "仅支持 user/assistant 角色")
    private String role;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "单条消息过长")
    private String content;
}
