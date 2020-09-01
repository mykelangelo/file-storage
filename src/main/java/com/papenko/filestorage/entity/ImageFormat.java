package com.papenko.filestorage.entity;

import java.util.Arrays;

public enum ImageFormat {
    I_01(".jpg"),
    I_02(".jpeg"),
    I_03(".jpe"),
    I_04(".jif"),
    I_05(".jfif"),
    I_06(".jfi"),
    I_07(".png"),
    I_08(".eps"),
    I_09(".webp"),
    I_10(".tiff"),
    I_11(".tif"),
    I_12(".psd"),
    I_13(".raw"),
    I_14(".arw"),
    I_15(".cr2"),
    I_16(".nrw"),
    I_17(".k25"),
    I_18(".bmp"),
    I_19(".dib"),
    I_20(".heif"),
    I_21(".heic"),
    I_22(".ind"),
    I_23(".indd"),
    I_24(".indt"),
    I_25(".jp2"),
    I_26(".j2k"),
    I_27(".jpf"),
    I_28(".jpx"),
    I_29(".jpm"),
    I_30(".mj2"),
    I_31(".svg"),
    I_32(".svgz"),
    I_33(".ai");

    private final String extension;

    ImageFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static boolean isImageFormat(String fileName) {
        final String lowerCase = fileName.toLowerCase();
        return Arrays.stream(values())
                .anyMatch(imageFormat -> lowerCase.endsWith(imageFormat.extension));
    }
}
