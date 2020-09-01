package com.papenko.filestorage.exception;

public class FileUpload400Exception extends RuntimeException {
    public FileUpload400Exception(String errorMessage) {
        super(errorMessage);
    }
}
