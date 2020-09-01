package com.papenko.filestorage.entity;

import java.util.Arrays;

public enum AudioFormat {
    A_01(".3gp"),
    A_02(".aa"),
    A_03(".aac"),
    A_04(".aax"),
    A_05(".act"),
    A_06(".aiff"),
    A_07(".alac"),
    A_08(".amr"),
    A_09(".ape"),
    A_10(".au"),
    A_11(".awb"),
    A_12(".dct"),
    A_13(".dss"),
    A_14(".dvf"),
    A_15(".flac"),
    A_16(".gsm"),
    A_17(".iklax"),
    A_18(".ivs"),
    A_19(".m4a"),
    A_20(".m4b"),
    A_21(".m4p"),
    A_22(".mmf"),
    A_23(".mp3"),
    A_24(".mpc"),
    A_25(".msv"),
    A_26(".nmf"),
    A_27(".ogg"),
    A_28(".oga"),
    A_29(".mogg"),
    A_30(".opus"),
    A_31(".ra"),
    A_32(".rm"),
    A_33(".raw"),
    A_34(".rf64"),
    A_35(".sln"),
    A_36(".tta"),
    A_37(".voc"),
    A_38(".vox"),
    A_39(".wav"),
    A_40(".wma"),
    A_41(".wv"),
    A_42(".webm"),
    A_43(".8svx"),
    A_44(".cda");

    private final String extension;

    AudioFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static boolean isAudioFormat(String fileName) {
        final String lowerCase = fileName.toLowerCase();
        return Arrays.stream(values())
                .anyMatch(audioFormat -> lowerCase.endsWith(audioFormat.extension));
    }
}
