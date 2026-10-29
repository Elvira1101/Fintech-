package com.fintech.auth.dto;

public record AuthResponse(String email, String role, String tokenType, String accessToken) {
}