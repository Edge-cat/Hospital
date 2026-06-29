package com.neusoft.hospital.service.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class WxAuthClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${hospital.wx.demo:true}")
    private boolean wxDemo;

    @Value("${hospital.wx.app-id:}")
    private String appId;

    @Value("${hospital.wx.app-secret:}")
    private String appSecret;

    public WxSession code2Session(String code) {
        if (wxDemo || !org.springframework.util.StringUtils.hasText(appId)) {
            WxSession session = new WxSession();
            session.setOpenid("demo-openid-" + (code != null ? code : "guest"));
            session.setSessionKey("demo-session");
            return session;
        }
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);
        try {
            String body = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(body);
            if (node.hasNonNull("errcode") && node.get("errcode").asInt() != 0) {
                throw new IllegalStateException(node.path("errmsg").asText("微信授权失败"));
            }
            WxSession session = new WxSession();
            session.setOpenid(node.path("openid").asText(null));
            session.setSessionKey(node.path("session_key").asText(null));
            return session;
        } catch (Exception ex) {
            throw new IllegalStateException("微信 jscode2session 调用失败: " + ex.getMessage());
        }
    }

    public static class WxSession {
        private String openid;
        private String sessionKey;

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public String getSessionKey() {
            return sessionKey;
        }

        public void setSessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
        }
    }
}
