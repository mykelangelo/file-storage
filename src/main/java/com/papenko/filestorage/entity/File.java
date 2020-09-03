package com.papenko.filestorage.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;
import java.util.Objects;

@Document(indexName = "file")
public class File {
    @Id
    private final String id;
    private final String name;
    /**
     * file size in bytes
     */
    private final Long size;
    private final List<String> tags;

    public File(String id, String name, Long size, List<String> tags) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.tags = tags == null ? List.of() : List.copyOf(tags);
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

    public File withTags(List<String> newTags) {
        return new File(id, name, size, newTags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        File file = (File) o;
        return Objects.equals(id, file.id) &&
                Objects.equals(name, file.name) &&
                Objects.equals(size, file.size) &&
                Objects.equals(tags, file.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, size, tags);
    }
}
