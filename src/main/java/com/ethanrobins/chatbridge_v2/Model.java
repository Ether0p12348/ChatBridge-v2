package com.ethanrobins.chatbridge_v2;

import com.ethanrobins.chatbridge_v2.drivers.TranslateType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Enum representing ChatBridge AI Models.
 * <br><br><b>Models:</b>
 * <br>{@link Model#CHATBRIDGE_15}
 * @see TranslateType
 */
public enum Model {
    CHATBRIDGE_15("ft:gpt-4.1-mini-2025-04-14:avidzenith:chatbridge-15-1:CAoAHvVC", TranslateType.MESSAGE_V1, TranslateType.EMBED_V1);

    /**
     * The model's OpenAI Model Identifier
     */
    @Getter
    private @NotNull final String id;
    /**
     * Filter for which translate types are allowed to be used for this model
     * <br>{@link TranslateType}{@code []}

     */
    @Getter
    private final TranslateType[] allowedTypes;

    Model(@NotNull String id, TranslateType... allowedTypes) {
        this.id = id;
        this.allowedTypes = allowedTypes;
    }

    /**
     * The default model for this build - Set manually in {@link Model}
     * @return Default {@link Model}
     */
    public static Model getDefault() {
        return CHATBRIDGE_15;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
