package com.smartcane.api.security.oauth;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attrs;
    public NaverUserInfo(Map<String, Object> attrs) { this.attrs = attrs; }

    @Override public String getProvider() { return "NAVER"; }

    @Override
    public String getProviderId() {
        Map<String, Object> response = (Map<String, Object>) attrs.get("response");
        return response == null ? null : (String) response.get("id");
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attrs.get("response");
        return response == null ? null : (String) response.get("email");
    }

    @Override
    public String getNickname() {
        Map<String, Object> response = (Map<String, Object>) attrs.get("response");
        return response == null ? null : (String) response.get("nickname");
    }

    @Override public Map<String, Object> getAttributes() { return attrs; }
}
