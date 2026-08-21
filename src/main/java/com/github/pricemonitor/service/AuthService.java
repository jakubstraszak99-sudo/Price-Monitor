package com.github.pricemonitor.service;

import com.github.pricemonitor.request.UserRegisterRequest;

public interface AuthService {

    void registerUser(final UserRegisterRequest request);

    void verifyAccount(final String token);

}
