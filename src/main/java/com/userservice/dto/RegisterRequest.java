package com.userservice.dto;

import com.userservice.entity.Role;

public record RegisterRequest(
        String email,
        String password,
        Role role
) {}
