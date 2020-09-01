package com.papenko.filestorage.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Optional<String> firstTag = Optional.empty();
        if (name != null) {
            firstTag = defineFirstTagIfApplicable();
        }
        List<String> newTags;
        if (firstTag.isEmpty()) {
            newTags = tags;
        } else {
            newTags = tags == null ?
                    new ArrayList<>(1) :
                    new ArrayList<>(tags);
            newTags.add(firstTag.get());
        }
        this.tags = tags == null ?
                newTags :
                List.copyOf(newTags.stream()
                        .map(String::toLowerCase)
                        .distinct()
                        .collect(Collectors.toList()));
    }

    private Optional<String> defineFirstTagIfApplicable() {
        if (DocumentFormat.isDocumentFormat(name)) {
            return Optional.of("document");
        }
        if (VideoFormat.isVideoFormat(name)) {
            return Optional.of("video");
        }
        if (ImageFormat.isImageFormat(name)) {
            return Optional.of("image");
        }
        if (AudioFormat.isAudioFormat(name)) {
            return Optional.of("audio");
        }
        return Optional.empty();
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
