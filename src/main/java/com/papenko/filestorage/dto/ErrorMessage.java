package com.papenko.filestorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ErrorMessage implements ResponseEntityBody {
    private final boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String error;

    public ErrorMessage(String error) {
        this.success = false;
        this.error = error;
    }

    public ErrorMessage() {
        this.success = true;
        this.error = null;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}
