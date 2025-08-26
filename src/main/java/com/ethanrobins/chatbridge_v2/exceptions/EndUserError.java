package com.ethanrobins.chatbridge_v2.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;

import java.util.HashMap;
import java.util.Map;

@Getter
public class EndUserError {
    private final Exception exception;
    private final Map<DiscordLocale, String> localeMessages = new HashMap<>();

    public EndUserError(Exception exception, Map<DiscordLocale, String> localeMessages) {
        this.exception = exception;
        this.localeMessages.putAll(localeMessages);
    }

}
