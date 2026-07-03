package com.jobpilot.application.identity.dto;

public record OAuthCommand(String provider, String code, String redirectUri) {}
