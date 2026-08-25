package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.AccessTokenExpiryInfo;
import com.github.pricemonitor.model.dto.AuthTokenSet;

public interface AuthService {

    void registerUser(final String username, final String email, final String password);

    void verifyAccount(final String token);

    AuthTokenSet login(final String login, final String password);

    AccessTokenExpiryInfo refreshToken(final String refreshTokenValue);

}
