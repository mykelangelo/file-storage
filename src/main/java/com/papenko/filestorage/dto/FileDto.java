package com.papenko.filestorage.dto;

import com.papenko.filestorage.entity.File;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class FileDto {
    private final String id;
    @NotBlank(message = "file name is missing")
    private final String name;
    /**
     * file size in bytes
     */
    @NotNull(message = "file size is missing")
    @Min(value = 0, message = "file size is negative")
    private final Long size;
    private final Set<String> tags;

    public FileDto(String id, String name, Long size, Set<String> tags) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.tags = tags == null ? Set.of() : Set.copyOf(tags);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public Set<String> getTags() {
        return tags;
    }

    public File toFile() {
        return new File(id, name, size, tags);
    }
}
