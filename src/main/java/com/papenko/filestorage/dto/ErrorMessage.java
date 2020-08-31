package com.papenko.filestorage.dto;

public class ErrorMessage extends SuccessStatus {
    private final String error;

    public ErrorMessage(Boolean success, String error) {
        super(success);
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
