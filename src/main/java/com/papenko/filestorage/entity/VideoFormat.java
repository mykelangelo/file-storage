package com.papenko.filestorage.entity;

import java.util.Arrays;

public enum VideoFormat {
    V_01(".webm"),
    V_02(".mkv"),
    V_03(".flv"),
    V_04(".f4v"),
    V_05(".vob"),
    V_06(".ogv"),
    V_07(".ogg"),
    V_08(".drc"),
    V_09(".gif"),
    V_10(".gifv"),
    V_11(".mng"),
    V_12(".avi"),
    V_13(".mts"),
    V_14(".m2ts"),
    V_15(".ts"),
    V_16(".mov"),
    V_17(".qt"),
    V_18(".wmv"),
    V_19(".yuv"),
    V_20(".rm"),
    V_21(".rmvb"),
    V_22(".viv"),
    V_23(".asf"),
    V_24(".amv"),
    V_25(".mp4"),
    V_26(".m4p"),
    V_27(".m4v"),
    V_28(".mpg"),
    V_29(".mp2"),
    V_30(".mpeg"),
    V_31(".m2v"),
    V_32(".m4v"),
    V_33(".svi"),
    V_34(".3gp"),
    V_35(".3g2"),
    V_36(".mxf"),
    V_37(".roq"),
    V_38(".nsv"),
    V_39(".f4p"),
    V_40(".f4a"),
    V_41(".f4b");

    private final String extension;

    VideoFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static boolean isVideoFormat(String fileName) {
        final String lowerCase = fileName.toLowerCase();
        return Arrays.stream(values())
                .anyMatch(videoFormat -> lowerCase.endsWith(videoFormat.extension));
    }
}
