package com.papenko.filestorage.entity;

import com.papenko.filestorage.constant.Index;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Objects;
import java.util.Set;

@Document(indexName = Index.FILE)
public class File {
    @Id
    private final String id;
    private final String name;
    /**
     * file size in bytes
     */
    private final Long size;
    private final Set<String> tags;

    public File(String id, String name, Long size, Set<String> tags) {
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

    public File withTags(Set<String> newTags) {
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

    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", tags=" + tags +
                '}';
    }
}
