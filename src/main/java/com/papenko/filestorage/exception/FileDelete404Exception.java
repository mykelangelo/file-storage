package com.papenko.filestorage.exception;

public class FileDelete404Exception extends RuntimeException {
    public FileDelete404Exception() {
        super("file not found");
    }
}
