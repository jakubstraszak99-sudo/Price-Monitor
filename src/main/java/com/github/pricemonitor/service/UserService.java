package com.github.pricemonitor.service;

import com.github.pricemonitor.request.ChangePasswordRequest;
import com.github.pricemonitor.request.ResetPasswordRequest;

public interface UserService {

    void resetPassword(final ResetPasswordRequest request);

    void changePassword(final ChangePasswordRequest request);

}
