package com.papenko.filestorage.entity;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileTest {

    @Test
    void tagsListIsImmutable_shouldPerformCopyingInConstructor() {
        Set<String> tags = new HashSet<>();
        tags.add("text");
        File file = new File("id0", "filename", 1L, tags);

        tags.add("yolo");

        assertEquals(1, file.getTags().size());
        assertEquals(Set.of("text"), file.getTags());
    }

    @Test
    void tagsListIsImmutable_shouldBeImpossibleToAddNewTag() {
        Set<String> tags = new HashSet<>();
        tags.add("text");
        File file = new File("id0", "filename.txt", 1L, tags);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> file.getTags().add("yo"));
    }

    @Test
    void tagsListIsImmutable_shouldPerformCopyingInWithTags() {
        Set<String> tags = new HashSet<>();
        tags.add("text");
        File file = new File("id0", "filename.txt", 1L, null);
        File withTags = file.withTags(tags);

        tags.add("yolo");

        assertEquals(1, withTags.getTags().size());
        assertThat(withTags.getTags()).containsExactlyInAnyOrder("text");
    }
}
