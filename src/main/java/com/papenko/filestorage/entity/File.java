package com.papenko.filestorage.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

@Document(indexName = "file")
public class File {
    @Id
    private final String id;
    private final String name;
    /**
     * file size in bytes
     */
    private final Long size;
    private List<String> tags;

    public File(String id, String name, Long size, List<String> tags) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.tags = tags == null ? null : List.copyOf(tags);
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = List.copyOf(tags);
    }
}
