package com.github.pricemonitor.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticationUtil {

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    public static final String ACCESS_TOKEN_PATH = "/";
    public static final String REFRESH_TOKEN_PATH = "/api/v1/auth/refresh";

    public static final String VERIFICATION_TOKEN_PATH = "/verify?token=";
    public static final String PASSWORD_RESET_PATH = "/reset-password?token=";

}
