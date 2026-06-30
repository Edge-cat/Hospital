package com.neusoft.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConsultResponse {

    private String reply;

    private String disclaimer;

    /** demo：未配置 API Key 时的本地模拟回复 */
    private boolean demoMode;
}
