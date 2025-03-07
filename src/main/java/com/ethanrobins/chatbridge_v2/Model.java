package com.ethanrobins.chatbridge_v2;

import com.ethanrobins.chatbridge_v2.drivers.TranslateType;

/**
 * Enum representing ChatBridge AI Models.
 * <br>TODO: Implement the TranslateType filter to TranslateType usages.
 * <br>TODO: Complete implementation of Models in bot.
 * <br>TODO: Complete update to chatbridge-13 (keep logic for chatbridge-12 support)
 * <br><br><b>Models:</b>
 * <br>{@link Model#CHATBRIDGE_12}
 * <br>{@link Model#CHATBRIDGE_13}
 * @see TranslateType
 */
public enum Model {
    /**
     * Initial <b>tracked</b> model
     * <br><br><b>Allowed {@link TranslateType}:</b>
     * <br>{@link TranslateType#PLAIN}
     * <br>{@link TranslateType#DECORATED}
     * <br>{@link TranslateType#CAPTION}
     */
    CHATBRIDGE_12("ft:gpt-4o-mini-2024-07-18:smok2314:chatbridge-12:B5ipZVOs", TranslateType.PLAIN, TranslateType.DECORATED, TranslateType.CAPTION),
    /**
     * <b>Change Log</b> (from {@link Model#CHATBRIDGE_12}):
     * <ul>
     *     <li>Merged {@link TranslateType#DECORATED} and {@link TranslateType#PLAIN} to {@link TranslateType#TRANSLATE}</li>
     *     <li>Refined {@link TranslateType#CAPTION}</li>
     * </ul>
     * <br><b>Allowed {@link TranslateType}</b>
     * <br>{@link TranslateType#TRANSLATE}
     * <br>{@link TranslateType#CAPTION}
     */
    CHATBRIDGE_13("ft:gpt-4o-mini-2024-07-18:smok2314:chatbridge-13:B8E7YvCr", TranslateType.TRANSLATE, TranslateType.CAPTION);

    /**
     * The model's OpenAI Model Identifier
     */
    private final String modelId;
    /**
     * Filter for which types are allowed to be used for the model
     */
    private final TranslateType[] allowedTypes;

    Model(String modelId, TranslateType... allowedTypes) {
        this.modelId = modelId;
        this.allowedTypes = allowedTypes;
    }

    /**
     * @return The model's OpenAI Model Identifier
     */
    public String getId() {
        return this.modelId;
    }

    /**
     * Filter for which translate types are allowed to be used for this model
     * @return {@link TranslateType}{@code []}
     */
    public TranslateType[] getAllowedTypes() {
        return this.allowedTypes;
    }

    /**
     * The default model for this build - Set manually in {@link Model}
     * @return Default {@link Model}
     */
    public static Model getDefault() {
        return CHATBRIDGE_13;
    }

    @Override
    public String toString() {
        return this.modelId;
    }
}
