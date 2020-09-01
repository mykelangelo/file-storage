package com.papenko.filestorage.exception;

public class FileUpdateTags404Exception extends RuntimeException {
    public FileUpdateTags404Exception() {
        super("file not found");
    }
}
