package com.github.pricemonitor.model.dto;

import java.util.UUID;

public record User(
        UUID publicId,
        String username,
        String email,
        Boolean emailAlertsEnabled
) {}
