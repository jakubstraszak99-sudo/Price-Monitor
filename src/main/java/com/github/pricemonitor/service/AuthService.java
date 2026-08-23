package com.github.pricemonitor.service;

import com.github.pricemonitor.request.TokenRefreshRequest;
import com.github.pricemonitor.request.UserLoginRequest;
import com.github.pricemonitor.request.UserRegisterRequest;
import com.github.pricemonitor.response.AuthResponse;

public interface AuthService {

    void registerUser(final UserRegisterRequest request);

    void verifyAccount(final String token);

    AuthResponse login(final UserLoginRequest request);

    AuthResponse refreshToken(final TokenRefreshRequest request);

}
