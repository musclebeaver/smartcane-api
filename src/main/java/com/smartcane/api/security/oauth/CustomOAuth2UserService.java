package com.smartcane.api.security.oauth;

import com.smartcane.api.domain.identity.entity.User;
import com.smartcane.api.domain.identity.entity.UserAuth;
import com.smartcane.api.domain.identity.repository.UserAuthRepository;
import com.smartcane.api.domain.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "kakao" | "naver"
        OAuth2UserInfo info = switch (registrationId.toLowerCase()) {
            case "kakao" -> new KakaoUserInfo(oAuth2User.getAttributes());
            case "naver" -> new NaverUserInfo(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };

        String providerId = info.getProviderId();
        if (providerId == null || providerId.isBlank()) {
            throw new OAuth2AuthenticationException("Missing providerId from " + info.getProvider());
        }

        UserAuth.Provider provider = UserAuth.Provider.valueOf(info.getProvider());

        // 1) (provider, providerId)로 기존 연결 찾기
        User user = userAuthRepository.findByProviderAndProviderId(provider, providerId)
                .map(UserAuth::getUser)
                .orElseGet(() -> {
                    // 2) 없으면 User 신규 생성 (이메일이 없을 수 있으므로 fallback 제공)
                    String email = info.getEmail();
                    User u = (email != null) ?
                            userRepository.findByEmail(email).orElse(null) : null;

                    if (u == null) {
                        u = new User();
                        u.setEmail(email != null ? email : provider.name().toLowerCase() + "_" + providerId + "@smartcane.local");
                        u.setNickname(info.getNickname());
                        userRepository.save(u);
                    }

                    // 3) UserAuth 소셜 연결 저장
                    UserAuth link = new UserAuth();
                    link.setUser(u);
                    link.setProvider(provider);
                    link.setProviderId(providerId);
                    userAuthRepository.save(link);

                    return u;
                });

        // 4) 성공 핸들러가 바로 userId를 사용할 수 있도록 attributes에 주입
        Map<String, Object> enriched = new HashMap<>(oAuth2User.getAttributes());
        enriched.put("sc_user_id", user.getId());
        enriched.put("sc_email", user.getEmail());

        // 반환할 이름 속성 (provider definition의 userNameAttributeName)
        String nameAttr = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), enriched, nameAttr);
    }
}
