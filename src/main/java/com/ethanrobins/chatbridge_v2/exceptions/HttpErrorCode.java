package com.ethanrobins.chatbridge_v2.exceptions;

import lombok.Getter;

@Getter
public class HttpErrorCode extends Exception {
    private final int statusCode;

    public HttpErrorCode(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpErrorCode(int statusCode) {
        super("Http Error Code: " + statusCode);
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "TranslationException{" +
                "statusCode=" + statusCode +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
