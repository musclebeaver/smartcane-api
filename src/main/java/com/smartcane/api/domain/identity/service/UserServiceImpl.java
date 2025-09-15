package com.smartcane.api.domain.identity.service;

import com.smartcane.api.domain.identity.dto.UserResponse;
import com.smartcane.api.domain.identity.dto.UserSignupRequest;
import com.smartcane.api.domain.identity.dto.UserUpdateRequest;
import com.smartcane.api.domain.identity.entity.User;
import com.smartcane.api.domain.identity.entity.UserAuth;
import com.smartcane.api.domain.identity.mapper.UserMapper;
import com.smartcane.api.domain.identity.repository.UserAuthRepository;
import com.smartcane.api.domain.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse signup(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        // 1) User 생성
        User user = userMapper.toEntity(request);
        user = userRepository.save(user);

        // 2) UserAuth 생성 (LOCAL + passwordHash)
        UserAuth auth = new UserAuth();
        auth.setUser(user);
        auth.setProvider(UserAuth.Provider.LOCAL);
        auth.setPasswordHash(passwordEncoder.encode(request.password()));
        userAuthRepository.save(auth);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (request.nickname() != null) user.setNickname(request.nickname());
        if (request.birthDate() != null) user.setBirthDate(request.birthDate());
        return userMapper.toResponse(user);
    }
}
