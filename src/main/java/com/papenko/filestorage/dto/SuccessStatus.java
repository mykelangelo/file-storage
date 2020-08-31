package com.papenko.filestorage.dto;

public class SuccessStatus implements ResponseEntityBody {
    private final boolean success;

    public SuccessStatus(boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }
}
