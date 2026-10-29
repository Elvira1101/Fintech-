package com.fintech.auth.dto;

public record OtpChallengeResponse(String email, boolean otpRequired, String message) {
}