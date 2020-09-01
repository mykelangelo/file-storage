package com.papenko.filestorage.entity;

import java.util.Arrays;

public enum DocumentFormat {
    D_01(".doc"),
    D_02(".docx"),
    D_03(".htm"),
    D_04(".html"),
    D_05(".odt"),
    D_06(".pdf"),
    D_07(".xls"),
    D_08(".xlsx"),
    D_09(".ods"),
    D_10(".ppt"),
    D_11(".pptx"),
    D_12(".txt");

    private final String extension;

    DocumentFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static boolean isDocumentFormat(String fileName) {
        final String lowerCase = fileName.toLowerCase();
        return Arrays.stream(values())
                .anyMatch(documentFormat -> lowerCase.endsWith(documentFormat.extension));
    }
}
