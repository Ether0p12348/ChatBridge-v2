package com.ethanrobins.chatbridge_v2.drivers;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
public enum SafetyLevel {
    SAFE("safe"),
    EXPLICIT("explicit");

    private final String id;

    SafetyLevel(@NotNull String id) {
        this.id = id;
    }

    public static @Nullable SafetyLevel fromIdOrNull(@NotNull String id) {
        for (SafetyLevel l : SafetyLevel.values()) {
            if (l.id.equals(id)) {
                return l;
            }
        }
        return null;
    }

    public static @NotNull SafetyLevel fromId(@NotNull String id) {
        return Objects.requireNonNullElse(fromIdOrNull(id), SAFE);
    }

    @Override
    public String toString() {
        return this.id;
    }
}
