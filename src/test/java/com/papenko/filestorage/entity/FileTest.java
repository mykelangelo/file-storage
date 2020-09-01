package com.papenko.filestorage.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileTest {

    @Test
    void tagsListIsImmutable_shouldPerformCopyingInConstructor() {
        List<String> tags = new ArrayList<>();
        tags.add("text");
        File file = new File("id0", "filename", 1L, tags);

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
    void tagsListIsImmutable_shouldPerformCopyingInWithTags() {
        List<String> tags = new ArrayList<>();
        tags.add("text");
        File file = new File("id0", "filename.txt", 1L, null);
        File withTags = file.withTags(tags);

        tags.add("yolo");

        assertEquals(2, withTags.getTags().size());
        assertThat(withTags.getTags()).containsExactlyInAnyOrder("document", "text");
    }

    @Test
    void shouldRemoveDuplicateTagsInConstructor_whenLetterCaseIsDifferent() {
        File file = new File(null, "name", 0L, List.of("Duplicate", "duplicate", "duplicate"));

        assertEquals(1, file.getTags().size());
        assertEquals("duplicate", file.getTags().get(0));
    }

    @Test
    void shouldAddDocumentTagForRandomDocumentFileExtension_whenTagsWereNull() {
        File file = new File(null, "name" + DocumentFormat.D_02.getExtension(), 0L, null);

        assertEquals(1, file.getTags().size());
        assertEquals("document", file.getTags().get(0));
    }

    @Test
    void shouldAddDocumentTagForRandomDocumentFileExtension_whenTagsWereEmptyList() {
        File file = new File(null, "name" + DocumentFormat.D_07.getExtension(), 0L, List.of());

        assertEquals(1, file.getTags().size());
        assertEquals("document", file.getTags().get(0));
    }

    @Test
    void shouldAddDocumentTagForRandomDocumentFileExtension_whenTagsContainedOneDifferentElement() {
        File file = new File(null, "name" + DocumentFormat.D_12.getExtension(), 0L, List.of("lol"));

        assertEquals(2, file.getTags().size());
        assertThat(file.getTags()).containsExactlyInAnyOrder("document", "lol");
    }

    @Test
    void shouldNotAddDocumentTagForRandomDocumentFileExtension_whenTagsContainedOneSameElement() {
        File file = new File(null, "name" + DocumentFormat.D_11.getExtension(), 0L, List.of("Document"));

        assertEquals(1, file.getTags().size());
        assertEquals("document", file.getTags().get(0));
    }

    @Test
    void shouldAddVideoTagForRandomVideoFileExtension_whenTagsWereNull() {
        File file = new File(null, "name" + VideoFormat.V_01.getExtension(), 0L, null);

        assertEquals(1, file.getTags().size());
        assertEquals("video", file.getTags().get(0));
    }

    @Test
    void shouldAddVideoTagForRandomVideoFileExtension_whenTagsWereEmptyList() {
        File file = new File(null, "name" + VideoFormat.V_07.getExtension(), 0L, List.of());

        assertEquals(1, file.getTags().size());
        assertEquals("video", file.getTags().get(0));
    }

    @Test
    void shouldAddVideoTagForRandomVideoFileExtension_whenTagsContainedOneDifferentElement() {
        File file = new File(null, "name" + VideoFormat.V_10.getExtension(), 0L, List.of("lol"));

        assertEquals(2, file.getTags().size());
        assertThat(file.getTags()).containsExactlyInAnyOrder("video", "lol");
    }

    @Test
    void shouldNotAddVideoTagForRandomVideoFileExtension_whenTagsContainedOneSameElement() {
        File file = new File(null, "name" + VideoFormat.V_41.getExtension(), 0L, List.of("Video"));

        assertEquals(1, file.getTags().size());
        assertEquals("video", file.getTags().get(0));
    }

    @Test
    void shouldAddImageTagForRandomImageFileExtension_whenTagsWereNull() {
        File file = new File(null, "name" + ImageFormat.I_03.getExtension(), 0L, null);

        assertEquals(1, file.getTags().size());
        assertEquals("image", file.getTags().get(0));
    }

    @Test
    void shouldAddImageTagForRandomImageFileExtension_whenTagsWereEmptyList() {
        File file = new File(null, "name" + ImageFormat.I_05.getExtension(), 0L, List.of());

        assertEquals(1, file.getTags().size());
        assertEquals("image", file.getTags().get(0));
    }

    @Test
    void shouldAddImageTagForRandomImageFileExtension_whenTagsContainedOneDifferentElement() {
        File file = new File(null, "name" + ImageFormat.I_24.getExtension(), 0L, List.of("lol"));

        assertEquals(2, file.getTags().size());
        assertThat(file.getTags()).containsExactlyInAnyOrder("image", "lol");
    }

    @Test
    void shouldNotAddImageTagForRandomImageFileExtension_whenTagsContainedOneSameElement() {
        File file = new File(null, "name" + ImageFormat.I_13.getExtension(), 0L, List.of("Image"));

        assertEquals(1, file.getTags().size());
        assertEquals("image", file.getTags().get(0));
    }

    @Test
    void shouldAddAudioTagForRandomAudioFileExtension_whenTagsWereNull() {
        File file = new File(null, "name" + AudioFormat.A_02.getExtension(), 0L, null);

        assertEquals(1, file.getTags().size());
        assertEquals("audio", file.getTags().get(0));
    }

    @Test
    void shouldAddAudioTagForRandomAudioFileExtension_whenTagsWereEmptyList() {
        File file = new File(null, "name" + AudioFormat.A_23.getExtension(), 0L, List.of());

        assertEquals(1, file.getTags().size());
        assertEquals("audio", file.getTags().get(0));
    }

    @Test
    void shouldAddAudioTagForRandomAudioFileExtension_whenTagsContainedOneDifferentElement() {
        File file = new File(null, "name" + AudioFormat.A_44.getExtension(), 0L, List.of("lol"));

        assertEquals(2, file.getTags().size());
        assertThat(file.getTags()).containsExactlyInAnyOrder("audio", "lol");
    }

    @Test
    void shouldNotAddAudioTagForRandomAudioFileExtension_whenTagsContainedOneSameElement() {
        File file = new File(null, "name" + AudioFormat.A_17.getExtension(), 0L, List.of("Audio"));

        assertEquals(1, file.getTags().size());
        assertEquals("audio", file.getTags().get(0));
    }
}
