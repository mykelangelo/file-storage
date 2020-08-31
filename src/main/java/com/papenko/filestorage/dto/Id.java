package com.papenko.filestorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Id implements ResponseEntityBody {
    @JsonProperty("ID")
    private final String id;

    public Id(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
