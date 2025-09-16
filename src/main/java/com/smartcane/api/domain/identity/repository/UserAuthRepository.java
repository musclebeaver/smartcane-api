package com.smartcane.api.domain.identity.repository;

import com.smartcane.api.domain.identity.entity.User;
import com.smartcane.api.domain.identity.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByUserAndProvider(User user, UserAuth.Provider provider);

    Optional<UserAuth> findByRefreshTokenHash(String refreshTokenHash);

    /** (provider, providerId)로 소셜 연결 찾기 */
    Optional<UserAuth> findByProviderAndProviderId(UserAuth.Provider provider, String providerId);
}
