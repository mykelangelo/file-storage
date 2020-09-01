package com.papenko.filestorage.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.ArrayList;
import java.util.List;
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
    private List<String> tags;

    public File(String id, String name, Long size, List<String> tags) {
        this.id = id;
        this.name = name;
        this.size = size;
        if (name != null) {
            addTagIfApplicable();
        }
        List<String> newTags;
        if (this.tags == null) {
            newTags = tags;
        } else {
            newTags = tags == null ?
                    new ArrayList<>(this.tags.size()) :
                    new ArrayList<>(tags);
            newTags.addAll(this.tags);
        }
        this.tags = tags == null ?
                this.tags :
                List.copyOf(newTags.stream()
                        .map(String::toLowerCase)
                        .distinct()
                        .collect(Collectors.toList()));
    }

    private void addTagIfApplicable() {
        if (DocumentFormat.isDocumentFormat(name)) {
            addFirstTag("document");
            return;
        }
        if (VideoFormat.isVideoFormat(name)) {
            addFirstTag("video");
            return;
        }
        if (ImageFormat.isImageFormat(name)) {
            addFirstTag("image");
            return;
        }
        if (AudioFormat.isAudioFormat(name)) {
            addFirstTag("audio");
        }
    }

    private void addFirstTag(String tag) {
        tags = List.of(tag);
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
