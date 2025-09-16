package com.smartcane.api.security.oauth;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attrs;
    public KakaoUserInfo(Map<String, Object> attrs) { this.attrs = attrs; }

    @Override public String getProvider() { return "KAKAO"; }

    @Override
    public String getProviderId() {
        Object id = attrs.get("id"); // Long
        return id == null ? null : String.valueOf(id);
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attrs.get("kakao_account");
        return kakaoAccount == null ? null : (String) kakaoAccount.get("email");
    }

    @Override
    public String getNickname() {
        Map<String, Object> profile = null;
        Map<String, Object> kakaoAccount = (Map<String, Object>) attrs.get("kakao_account");
        if (kakaoAccount != null) profile = (Map<String, Object>) kakaoAccount.get("profile");
        return profile == null ? null : (String) profile.get("nickname");
    }

    @Override public Map<String, Object> getAttributes() { return attrs; }
}
