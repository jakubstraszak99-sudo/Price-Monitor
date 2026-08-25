package com.github.pricemonitor.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaUtil {

    public static final String REGISTRATION_TOPIC = "user-registration-topic";
    public static final String PASSWORD_RESET_TOPIC = "password-reset-topic";

    public static final String EMAIL_SENDER_GROUP = "email-sender-group";

}
