package com.ethanrobins.chatbridge_v2.utils;

import java.security.SecureRandom;

public class RandomString {
    public static String generate(int length, Content... contents) {
        StringBuilder allowedCharsBuilder = new StringBuilder();
        for (Content c : contents) {
            allowedCharsBuilder.append(c.content);
        }
        if (allowedCharsBuilder.isEmpty()) {
            allowedCharsBuilder = new StringBuilder(Content.ALL.content);
        }
        String allowedChars = allowedCharsBuilder.toString();

        StringBuilder randomString = new StringBuilder();
        SecureRandom random = new SecureRandom();
        int maxIndex = allowedChars.length() - 1;

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(maxIndex + 1);
            randomString.append(allowedChars.charAt(index));
        }

        return randomString.toString();
    }

    public enum Content {
        ALL("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~!@#$%^&*"),
        LOWERCASE("abcdefghijklmnopqrstuvwxyz"),
        UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        NUMBERS("0123456789"),
        SYMBOLS("-_.~!@#$%^&*"),
        HEX("0123456789abcdef");

        private String content;

        Content(String content) {
            this.content = content;
        }
    }
}
