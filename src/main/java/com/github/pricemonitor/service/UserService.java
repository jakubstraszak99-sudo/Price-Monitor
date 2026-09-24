package com.github.pricemonitor.service;

import com.github.pricemonitor.model.dto.User;
import com.github.pricemonitor.model.entity.UserEntity;

import java.util.UUID;

public interface UserService {

    UserEntity getUserEntity(final UUID publicId);

    User getUser(final UUID publicId);

    User updateSettings(final UUID userPublicId, final boolean emailAlertsEnabled);

    void updatePassword(final UUID userPublicId, final String oldPassword, final String newPassword);

    void forgotPassword(final String email);

    void resetPassword(final String resetToken, final String newPassword);

}
