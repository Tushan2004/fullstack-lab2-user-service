package com.userservice.dto;

import com.userservice.entity.Role;

public record RegisterResponse(
        Long userId,
        Role role
) {}