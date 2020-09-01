package com.papenko.filestorage.exception;

public class FileDeleteTags400Exception extends RuntimeException {
    public FileDeleteTags400Exception() {
        super("tag not found on file");
    }
}
