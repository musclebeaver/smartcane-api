package com.smartcane.api.domain.identity.mapper;

import com.smartcane.api.domain.identity.dto.UserResponse;
import com.smartcane.api.domain.identity.dto.UserSignupRequest;
import com.smartcane.api.domain.identity.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "nickname", source = "nickname"),
            @Mapping(target = "birthDate", source = "birthDate"),
            @Mapping(target = "role", constant = "USER"),
            @Mapping(target = "status", constant = "ACTIVE")
    })
    User toEntity(UserSignupRequest request);

    UserResponse toResponse(User user);
}
