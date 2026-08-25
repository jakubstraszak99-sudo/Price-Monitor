package com.github.pricemonitor.service;

import com.github.pricemonitor.request.ChangePasswordRequest;
import com.github.pricemonitor.request.ResetPasswordRequest;

import java.util.UUID;

public interface UserService {

    void resetPassword(final ResetPasswordRequest request);

    void changePassword(final UUID userPublicId, final String newPassword);

}
