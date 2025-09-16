package com.smartcane.api.security.oauth;

import com.smartcane.api.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${smartcane.oauth2.success-redirect}")
    private String successRedirect;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        DefaultOAuth2User oUser = (DefaultOAuth2User) authentication.getPrincipal();

        Long userId = oUser.getAttribute("sc_user_id");
        String email = oUser.getAttribute("sc_email");

        String access  = jwtTokenProvider.generateAccessToken(userId, email, List.of("USER"));
        String refresh = jwtTokenProvider.generateRefreshToken(userId);

        String url = successRedirect
                + "?access=" + URLEncoder.encode(access, StandardCharsets.UTF_8)
                + "&refresh=" + URLEncoder.encode(refresh, StandardCharsets.UTF_8);

        response.setStatus(302);
        response.setHeader("Location", url);
    }
}
