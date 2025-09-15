package com.smartcane.api.domain.identity.service;

import com.smartcane.api.domain.identity.dto.*;
import com.smartcane.api.domain.identity.entity.User;
import com.smartcane.api.domain.identity.entity.UserAuth;
import com.smartcane.api.domain.identity.mapper.UserMapper;
import com.smartcane.api.domain.identity.repository.UserAuthRepository;
import com.smartcane.api.domain.identity.repository.UserRepository;
import com.smartcane.api.security.jwt.JwtTokenProvider;
import com.smartcane.api.security.util.SecureHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다."));

        UserAuth auth = userAuthRepository.findByUserAndProvider(user, UserAuth.Provider.LOCAL)
                .orElseThrow(() -> new IllegalStateException("로컬 로그인 정보가 없습니다."));

        if (!passwordEncoder.matches(request.password(), auth.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // Access / Refresh 발급
        List<String> roles = List.of(user.getRole().name());
        String accessToken  = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // refresh 해시 저장(회전 시작점). 기존 해시가 있어도 덮어씀
        auth.setRefreshTokenHash(SecureHashUtil.sha256Hex(refreshToken));

        return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
    }

    @Override
    public AuthResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        // 1) typ=refresh 강제
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        // 2) 토큰 유효성(서명/만료/issuer)은 JwtTokenProvider.isRefreshToken 내에서 검증됨
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 3) 소유자(UserAuth) 확인: 저장된 refresh 해시와 일치해야 함
        String hash = SecureHashUtil.sha256Hex(refreshToken);
        UserAuth auth = userAuthRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if (!Objects.equals(auth.getUser().getId(), userId)) {
            // 토큰의 subject와 DB 소유자가 다르면 위변조/재사용
            throw new IllegalArgumentException("리프레시 토큰 소유자가 일치하지 않습니다.");
        }

        // 4) 새 access/refresh 발급 + 해시 교체(회전)
        User user = auth.getUser();
        List<String> roles = List.of(user.getRole().name());
        String newAccess  = jwtTokenProvider.generateAccessToken(userId, user.getEmail(), roles);
        String newRefresh = jwtTokenProvider.generateRefreshToken(userId);
        auth.setRefreshTokenHash(SecureHashUtil.sha256Hex(newRefresh));

        return new AuthResponse(newAccess, newRefresh, userMapper.toResponse(user));
    }

    @Override
    public void logout(Long userId, String refreshToken) {
        if (userId == null || refreshToken == null || refreshToken.isBlank()) return;

        // typ=refresh가 아니면 무시(보수적)
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) return;

        String hash = SecureHashUtil.sha256Hex(refreshToken);
        userAuthRepository.findByRefreshTokenHash(hash).ifPresent(ua -> {
            if (Objects.equals(ua.getUser().getId(), userId)) {
                // 본인 소유 토큰만 무효화
                ua.setRefreshTokenHash(null);
            }
        });
    }
}
