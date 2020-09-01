package com.papenko.filestorage.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

class FileTest {

    @Test
    void tagsListIsImmutable_shouldPerformCopyingInConstructor() {
        List<String> tags = new ArrayList<>();
        tags.add("text");
        File file = new File("id0", "filename.txt", 1L, tags);

        tags.add("yolo");

        assertEquals(1, file.getTags().size());
        assertEquals("text", file.getTags().get(0));
    }

    @Test
    void tagsListIsImmutable_shouldBeImpossibleToAddNewTag() {
        List<String> tags = new ArrayList<>();
        tags.add("text");
        File file = new File("id0", "filename.txt", 1L, tags);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> file.getTags().add("yo"));
    }

    @Test
    void tagsListIsImmutable_shouldPerformCopyingInSetter() {
        List<String> tags = new ArrayList<>();
        tags.add("text");
        File file = new File("id0", "filename.txt", 1L, null);
        file.setTags(tags);

        tags.add("yolo");

        assertEquals(1, file.getTags().size());
        assertEquals("text", file.getTags().get(0));
    }
}
