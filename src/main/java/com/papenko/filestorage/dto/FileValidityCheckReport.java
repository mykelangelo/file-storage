package com.papenko.filestorage.dto;

public class FileValidityCheckReport {
    private final boolean valid;
    private final String errorMessage;

    public FileValidityCheckReport(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
