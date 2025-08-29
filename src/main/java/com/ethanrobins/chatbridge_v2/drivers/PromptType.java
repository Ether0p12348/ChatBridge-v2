package com.ethanrobins.chatbridge_v2.drivers;

import lombok.Getter;

@Getter
public enum PromptType {
    MESSAGE("pmpt_68b1534519a48193ad127ad0971a771d00c65cad751244f1");

    private final String id;

    PromptType(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
