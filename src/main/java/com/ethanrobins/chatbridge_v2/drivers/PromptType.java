package com.ethanrobins.chatbridge_v2.drivers;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public enum PromptType {
    MESSAGE("pmpt_68b501fb1e2c81938482bd862d30b2f1021303d66903eb25", "1"),
    EMBED("pmpt_68b50229644c8197ac944fa90aba9c1907e598a420ae7409", "1");

    private final @NotNull String id;
    private final @NotNull String version;

    PromptType(@NotNull String id, @NotNull String version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
