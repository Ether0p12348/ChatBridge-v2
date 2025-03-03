package com.ethanrobins.chatbridge_v2;

public enum TranslateType {
    PLAIN("Plain Translation."),
    DECORATED("Decorated Translation."),
    CAPTION("Caption Only.");

    private final String systemPrompt;

    TranslateType(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String toString() {
        return systemPrompt;
    }
}
