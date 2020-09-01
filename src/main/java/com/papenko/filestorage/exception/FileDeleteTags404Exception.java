package com.papenko.filestorage.exception;

public class FileDeleteTags404Exception extends RuntimeException {
    public FileDeleteTags404Exception() {
        super("file not found");
    }
}
