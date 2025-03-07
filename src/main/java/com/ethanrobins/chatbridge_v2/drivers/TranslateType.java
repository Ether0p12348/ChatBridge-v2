package com.ethanrobins.chatbridge_v2.drivers;

/**
 * Enum representing supported translation types for ChatBridge AI Models
 * <br><br><b>Translate Types:</b>
 * <br>{@link TranslateType#TRANSLATE}
 * <br>{@link TranslateType#PLAIN}
 * <br>{@link TranslateType#DECORATED}
 * <br>{@link TranslateType#CAPTION}
 * @see com.ethanrobins.chatbridge_v2.Model
 */
public enum TranslateType {
    /**
     * <b>WARNING:</b> Only used for models chatbridge-13 and later.
     * <br><br>Will translate the text given (requires a 'to' locale tag in parentheses before the text)
     * <br><b>Example:</b> {@code (ja)The string to be translated in Japanese.} &rarr; {@code 日本語に翻訳される文字列。}
     */
    TRANSLATE("Translate."),
    /**
     * <b>WARNING:</b> Only used for models chatbridge-12 and earlier.
     * <br><br>Will translate the text given without including the caption (requires a 'to' locale tag in parentheses before the text)
     * <br><b>Example:</b> {@code (ja)The string to be translated in Japanese.} &rarr; {@code 日本語に翻訳される文字列。}
     */
    PLAIN("Plain Translation."),
    /**
     * <b>WARNING:</b> Only used for models chatbridge-12 and earlier.
     * <br><br>Will translate the text given with the caption (requires a 'to' locale tag in parentheses before the text)
     * <br><b>Example:</b> {@code (ja)The string to be translated in Japanese.} &rarr; {@code __*英語 (en-US) → 日本語 (ja)*__\n日本語に翻訳される文字列。}
     */
    DECORATED("Decorated Translation."),
    /**
     * <b>WARNING:</b> Only used for models chatbridge-12 and later.
     * <br><br>Will get the locale caption from a given text (requires a 'to' locale tag in parentheses before the text)
     * <br><b>Example:</b> {@code (ja)The string to be translated in Japanese.} &rarr; {@code __*英語 (en-US) → 日本語 (ja)*__}
     */
    CAPTION("Caption Only.");

    /**
     * The system prompt used by ChatBridge AI Models
     */
    private final String systemPrompt;

    TranslateType(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    /**
     * @return the system prompt used by ChatBridge AI Models
     */
    public String getSystemPrompt() {
        return systemPrompt;
    }

    @Override
    public String toString() {
        return systemPrompt;
    }
}
