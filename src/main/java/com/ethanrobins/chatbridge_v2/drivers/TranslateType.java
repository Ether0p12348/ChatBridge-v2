package com.ethanrobins.chatbridge_v2.drivers;

import lombok.Getter;

/**
 * Enum representing supported translation types for ChatBridge AI Models
 * <br><br><b>Translate Types:</b>
 * @see com.ethanrobins.chatbridge_v2.Model
 */
@Getter
public enum TranslateType {
    MESSAGE_V1("CB_MESSAGE v1"),
    EMBED_V1("CB_EMBED v1");

    private final String systemPrompt;

    TranslateType(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Override
    public String toString() {
        return systemPrompt;
    }
}
