package com.papenko.filestorage.exception;

public class FileOperation404Exception extends RuntimeException {
    public FileOperation404Exception() {
        super("file not found");
    }
}
