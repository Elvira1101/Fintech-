package com.fintech.auth.mapper;

import com.fintech.auth.dto.RegistrationResponse;
import com.fintech.auth.entity.RoleName;
import com.fintech.auth.entity.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static RegistrationResponse toResponse(User user) {
        RoleName role = user.getRoles().iterator().next().getName();
        return new RegistrationResponse(user.getId(), user.getEmail(), role.name());
    }
}