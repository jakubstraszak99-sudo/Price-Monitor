package com.github.pricemonitor.service;

import com.github.pricemonitor.model.entity.UserEntity;

import java.util.UUID;

public interface UserService {

    UserEntity getUser(final UUID publicId);

    void updatePassword(final UUID userPublicId, final String oldPassword, final String newPassword);

    void forgotPassword(final String email);

    void resetPassword(final String resetToken, final String newPassword);

}
