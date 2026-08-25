package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.AccessTokenExpiryInfo;
import com.github.pricemonitor.request.UserLoginRequest;
import com.github.pricemonitor.request.UserRegisterRequest;
import com.github.pricemonitor.model.dto.AuthTokenSet;

public interface AuthService {

    void registerUser(final UserRegisterRequest request);

    void verifyAccount(final String token);

    AuthTokenSet login(final UserLoginRequest request);

    AccessTokenExpiryInfo refreshToken(final String refreshTokenValue);

}
