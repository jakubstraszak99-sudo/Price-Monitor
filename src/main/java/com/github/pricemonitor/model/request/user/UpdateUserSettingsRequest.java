package com.github.pricemonitor.model.request.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserSettingsRequest(
        @NotNull
        Boolean emailAlertsEnabled
) {}
