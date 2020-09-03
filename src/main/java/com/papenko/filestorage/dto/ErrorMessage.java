package com.papenko.filestorage.dto;

public class ErrorMessage {
    private final String error;

    public ErrorMessage(Boolean success, String error) {
        this.error = error;
    }
}
