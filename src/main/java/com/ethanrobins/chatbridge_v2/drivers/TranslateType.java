package com.ethanrobins.chatbridge_v2.drivers;

import lombok.Getter;

/**
 * Enum representing supported translation types for ChatBridge AI Models
 * <br><br><b>Translate Types:</b>
 * @see com.ethanrobins.chatbridge_v2.Model
 */
@Getter
public enum TranslateType {
    BUNDLE_V1("CB_BUNDLE v1");

    private final String systemPrompt;

    TranslateType(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    @Override
    public String toString() {
        return systemPrompt;
    }
}
