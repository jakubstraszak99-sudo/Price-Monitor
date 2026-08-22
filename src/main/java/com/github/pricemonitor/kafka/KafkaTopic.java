package com.github.pricemonitor.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopic {

    public static final String REGISTRATION_TOPIC = "user-registration-topic";
    public static final String PASSWORD_RESET_TOPIC = "password-reset-topic";

}
