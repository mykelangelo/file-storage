package com.papenko.filestorage.service;

import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.exception.FileOperation400Exception;
import com.papenko.filestorage.exception.FileOperation404Exception;
import com.papenko.filestorage.properties.FileExtensionProperties;
import com.papenko.filestorage.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
    @InjectMocks
    private FileService fileService;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private FileExtensionProperties fileExtensionProperties;

    @Test
    void uploadFile_shouldCallSaveMethodOfRepository() {
        File fileWithoutId = new File(null, "name", 0L, null);
        File fileWithId = new File("id1", "name", 0L, null);
        when(fileRepository.save(fileWithoutId)).thenReturn(fileWithId);

        fileService.uploadFile(fileWithoutId);

        verify(fileRepository).save(fileWithoutId);
    }

    @Test
    void getById_shouldReturnTheSameFileAsFound_whenFileIsFoundInDb() {
        final File file = new File("id0", "name", 0L, Set.of("tag1", "tag2"));
        when(fileRepository.findById("id0")).thenReturn(Optional.of(file));

        final File actual = fileService.getById("id0");

        assertEquals(file, actual);
    }

    @Test
    void getById_shouldThrowException_whenNoFileFoundBySuchId() {
        assertThatExceptionOfType(FileOperation404Exception.class)
                .isThrownBy(() -> fileService.getById("id0"))
                .withMessage("file not found");

        verify(fileRepository).findById("id0");
        verifyNoMoreInteractions(fileRepository);
    }

    @Test
    void delete_shouldThrowFileDelete404Exception_whenNoFileIsFoundBySuchId() {
        assertThatExceptionOfType(FileOperation404Exception.class)
                .isThrownBy(() -> fileService.delete("id0"))
                .withMessage("file not found");

        verify(fileRepository).existsById("id0");
        verifyNoMoreInteractions(fileRepository);
    }

    @Test
    void delete_shouldCallDeleteByIdMethodOfRepository() {
        when(fileRepository.existsById("id0")).thenReturn(true);

        fileService.delete("id0");

        verify(fileRepository).deleteById("id0");
    }

    @Test
    void addTags_shouldUpdateTagsById_whenFileExistsBySuchIdWithouttags() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, null)));
        final Set<String> tags = Set.of("tag1", "tag2", "tag3");

        fileService.addTags("id", tags);

        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(new File("id", "name", 0L, null).withTags(tags)));
    }

    @Test
    void addTags_shouldUpdateTagsById_whenFileExistsBySuchIdWithSomeUniqueTags() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, Set.of("yo"))));

        fileService.addTags("id", Set.of("tag1", "tag2", "tag3"));

        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(new File("id", "name", 0L, null).withTags(Set.of("tag1", "tag2", "tag3", "yo"))));
    }

    @Test
    void addTags_shouldUpdateTagsById_whenFileExistsBySuchIdWithSomeRepeatingTags() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, Set.of("yo", "yolo"))));

        fileService.addTags("id", Set.of("yo", "new tag"));

        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(new File("id", "name", 0L, null).withTags(Set.of("yolo", "new tag", "yo"))));
    }

    @Test
    void addTags_shouldThrowException_whenNoFileExistsBySuchId() {
        when(fileRepository.findById("id")).thenReturn(Optional.empty());

        assertThatExceptionOfType(FileOperation404Exception.class)
                .isThrownBy(() -> fileService.addTags("id", Set.of("tag1", "tag2", "tag3")))
                .withMessage("file not found");

        verifyNoMoreInteractions(fileRepository);
    }

    @Test
    void deleteTags_shouldThrowException_whenNoFileIsFoundById() {
        when(fileRepository.findById("id")).thenReturn(Optional.empty());

        assertThatExceptionOfType(FileOperation404Exception.class)
                .isThrownBy(() -> fileService.deleteTags("id", Set.of("tag1", "tag2")))
                .withMessage("file not found");

        verifyNoMoreInteractions(fileRepository);
    }

    @Test
    void deleteTags_shouldThrowException_whenNoTagsArePresentInFoundFile() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, null)));

        assertThatExceptionOfType(FileOperation400Exception.class)
                .isThrownBy(() -> fileService.deleteTags("id", Set.of("tag1", "tag2")))
                .withMessage("tag not found on file");
    }

    @Test
    void deleteTags_shouldThrowException_whenNotAllTagsArePresentInFoundFile() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, Set.of("tag1"))));

        assertThatExceptionOfType(FileOperation400Exception.class)
                .isThrownBy(() -> fileService.deleteTags("id", Set.of("tag1", "tag2")))
                .withMessage("tag not found on file");
    }

    @Test
    void deleteTags_shouldRemoveAllTags_whenAllTagsArePresentInFoundFile() {
        final File file = new File("id", "name", 0L, Set.of("tag1", "tag2"));
        when(fileRepository.findById("id")).thenReturn(Optional.of(file));

        fileService.deleteTags("id", Set.of("tag1", "tag2"));

        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(file.withTags(Set.of())));
    }

    @Test
    void deleteTags_shouldRemoveUnwantedTags_whenAllTagsArePresentInFoundFile() {
        final File file = new File("id", "name", 0L, Set.of("tag1", "tag2", "tag3"));
        when(fileRepository.findById("id")).thenReturn(Optional.of(file));

        fileService.deleteTags("id", Set.of("tag1", "tag2"));

        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(file.withTags(Set.of("tag3"))));
    }

    @Test
    void deleteTags_shouldRemoveUnwantedTags_whenAllTagsArePresentInFoundFileIgnoringCase() {
        final File file = new File("id", "name", 0L, Set.of("tag1", "tag2", "tag3"));
        when(fileRepository.findById("id")).thenReturn(Optional.of(file));

        fileService.deleteTags("id", Set.of("Tag1", "tAg2"));

        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(file.withTags(Set.of("tag3"))));
    }

    @Test
    void rectifyTags_shouldRemoveDuplicateTagsInConstructor_whenLetterCaseIsDifferent() {
        File old = new File(null, "name", 0L, Set.of("Duplicate", "duplicate"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("duplicate"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddDocumentTagForRandomDocumentFileExtension_whenTagsWereNull() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.document", Set.of("txt")));
        File old = new File(null, "name.txt", 0L, null);

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("document"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddDocumentTagForRandomDocumentFileExtension_whenTagsWereEmptyList() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.document", Set.of("epub")));
        File old = new File(null, "name.epub", 0L, Set.of());

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("document"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddDocumentTagForRandomDocumentFileExtension_whenTagsContainedOneDifferentElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.document", Set.of("pdf")));
        File old = new File(null, "name.pdf", 0L, Set.of("lol"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(2, correct.getTags().size());
        assertThat(correct.getTags()).containsExactlyInAnyOrder("document", "lol");
    }

    @Test
    void rectifyTags_shouldNotAddDocumentTagForRandomDocumentFileExtension_whenTagsContainedOneSameElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.document", Set.of("doc")));
        File old = new File(null, "name.doc", 0L, Set.of("Document"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("document"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddVideoTagForRandomVideoFileExtension_whenTagsWereNull() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.video", Set.of("mp4")));
        File old = new File(null, "name.mp4", 0L, null);

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("video"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddVideoTagForRandomVideoFileExtension_whenTagsWereEmptyList() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.video", Set.of("m4p")));
        File old = new File(null, "name.m4p", 0L, Set.of());

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("video"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddVideoTagForRandomVideoFileExtension_whenTagsContainedOneDifferentElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.video", Set.of("m4v")));
        File old = new File(null, "name.m4v", 0L, Set.of("lol"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(2, correct.getTags().size());
        assertThat(correct.getTags()).containsExactlyInAnyOrder("video", "lol");
    }

    @Test
    void rectifyTags_shouldNotAddVideoTagForRandomVideoFileExtension_whenTagsContainedOneSameElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.video", Set.of("mp2")));
        File old = new File(null, "name.mp2", 0L, Set.of("Video"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("video"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddImageTagForRandomImageFileExtension_whenTagsWereNull() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.image", Set.of("jpg")));
        File old = new File(null, "name.jpg", 0L, null);

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("image"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddImageTagForRandomImageFileExtension_whenTagsWereEmptyList() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.image", Set.of("jpeg")));
        File old = new File(null, "name.jpeg", 0L, Set.of());

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("image"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddImageTagForRandomImageFileExtension_whenTagsContainedOneDifferentElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.image", Set.of("jpe")));
        File old = new File(null, "name.jpe", 0L, Set.of("lol"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(2, correct.getTags().size());
        assertThat(correct.getTags()).containsExactlyInAnyOrder("image", "lol");
    }

    @Test
    void rectifyTags_shouldNotAddImageTagForRandomImageFileExtension_whenTagsContainedOneSameElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.image", Set.of("jif")));
        File old = new File(null, "name.jif", 0L, Set.of("Image"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("image"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddAudioTagForRandomAudioFileExtension_whenTagsWereNull() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.audio", Set.of("mp3")));
        File old = new File(null, "name.mp3", 0L, null);

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("audio"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddAudioTagForRandomAudioFileExtension_whenTagsWereEmptyList() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.audio", Set.of("iklax")));
        File old = new File(null, "name.iklax", 0L, Set.of());

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("audio"), correct.getTags());
    }

    @Test
    void rectifyTags_shouldAddAudioTagForRandomAudioFileExtension_whenTagsContainedOneDifferentElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.audio", Set.of("ivs")));
        File old = new File(null, "name.ivs", 0L, Set.of("lol"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(2, correct.getTags().size());
        assertThat(correct.getTags()).containsExactlyInAnyOrder("audio", "lol");
    }

    @Test
    void rectifyTags_shouldNotAddAudioTagForRandomAudioFileExtension_whenTagsContainedOneSameElement() {
        when(fileExtensionProperties.getExtensions()).thenReturn(Map.of("#.audio", Set.of("m4a")));
        File old = new File(null, "name.m4a", 0L, Set.of("Audio"));

        File correct = old.withTags(fileService.rectifyTags(old));

        assertEquals(1, correct.getTags().size());
        assertEquals(Set.of("audio"), correct.getTags());
    }
}
