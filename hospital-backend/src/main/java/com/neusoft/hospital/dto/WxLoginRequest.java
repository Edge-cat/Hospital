package com.neusoft.hospital.dto;

import lombok.Data;

@Data
public class WxLoginRequest {

    /** 微信 wx.login 返回的 code */
    private String code;
    /** 演示模式可传演示手机号，默认 13800138000 */
    private String phone;
}
