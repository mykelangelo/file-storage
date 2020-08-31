package com.papenko.filestorage.dto;

public class FileValidityCheckReport {
    private final boolean isValid;
    private final String errorMessage;

    public FileValidityCheckReport(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
