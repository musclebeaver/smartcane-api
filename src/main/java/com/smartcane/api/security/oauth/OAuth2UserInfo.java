package com.smartcane.api.security.oauth;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProvider();     // "KAKAO" | "NAVER"
    String getProviderId();   // 소셜 제공자 내부의 유니크 ID
    String getEmail();        // null 가능(동의항목에 따라)
    String getNickname();     // null 가능
    Map<String, Object> getAttributes();
}
