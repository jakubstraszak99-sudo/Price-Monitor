package com.github.pricemonitor.service;

import com.github.pricemonitor.request.ResetPasswordRequest;

public interface UserService {

    void resetPassword(final ResetPasswordRequest request);

}
