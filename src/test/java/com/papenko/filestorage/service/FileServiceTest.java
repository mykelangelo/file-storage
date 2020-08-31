package com.papenko.filestorage.service;

import com.papenko.filestorage.dto.FileValidityCheckReport;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {
    @InjectMocks
    private FileService fileService;
    @Mock
    private FileRepository fileRepository;

    @Test
    void isFileValid_shouldReturnTrueAndNull_whenValidFileIsPassed() {
        FileValidityCheckReport report = fileService.isFileValid(new File("id0", "name", 0L, null));

        assertTrue(report.isValid());
        assertNull(report.getErrorMessage());
    }

    @Test
    void isFileValid_shouldReturnFalseAndErrorMessage_whenNameIsMissing() {
        FileValidityCheckReport report = fileService.isFileValid(new File("id0", null, 0L, null));

        assertFalse(report.isValid());
        assertEquals("file name is missing", report.getErrorMessage());
    }

    @Test
    void isFileValid_shouldReturnFalseAndErrorMessage_whenSizeIsMissing() {
        FileValidityCheckReport report = fileService.isFileValid(new File("id0", "name", null, null));

        assertFalse(report.isValid());
        assertEquals("file size is missing", report.getErrorMessage());
    }

    @Test
    void isFileValid_shouldReturnFalseAndErrorMessage_whenSizeIsNegative() {
        FileValidityCheckReport report = fileService.isFileValid(new File("id0", "name", -1L, null));

        assertFalse(report.isValid());
        assertEquals("file size is negative", report.getErrorMessage());
    }

    @Test
    void uploadFile_shouldCallSaveMethodOfRepository() {
        File fileWithoutId = new File(null, "name", 0L, null);
        File fileWithId = new File("id1", "name", 0L, null);
        when(fileRepository.save(fileWithoutId)).thenReturn(fileWithId);

        fileService.uploadFile(fileWithoutId);

        verify(fileRepository).save(fileWithoutId);
    }

    @Test
    void delete_shouldCallDeleteByIdMethodOfRepository() {
        fileService.delete("id0");

        verify(fileRepository).deleteById("id0");
    }

    @Test
    void isPresentById_shouldReturnTrue_whenFileIsFoundInRepository() {
        doReturn(true).when(fileRepository).existsById("id0");

        final boolean presentById = fileService.isPresentById("id0");

        assertTrue(presentById);
    }

    @Test
    void isPresentById_shouldReturnFalse_whenFileIsNotFoundInRepository() {
        doReturn(false).when(fileRepository).existsById("id0");

        final boolean presentById = fileService.isPresentById("id0");

        assertFalse(presentById);
    }

    @Test
    void updateTags_shouldUpdateTagsById_whenFileExistsBySuchId() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, null)));

        fileService.updateTags("id", List.of("tag1", "tag2", "tag3"));

        verify(fileRepository).findById("id");
        final Optional<File> fileOptional = fileRepository.findById("id");
        assertTrue(fileOptional.isPresent());
        assertEquals(List.of("tag1", "tag2", "tag3"), fileOptional.get().getTags());
    }

    @Test
    void updateTags_shouldNotUpdateTagsById_whenNoFileExistsBySuchId() {
        when(fileRepository.findById("id")).thenReturn(Optional.empty());

        fileService.updateTags("id", List.of("tag1", "tag2", "tag3"));

        verifyNoMoreInteractions(fileRepository);
    }

    @Test
    void deleteTags_shouldReturnFalse_whenNoFileIsFoundById() {
        when(fileRepository.findById("id")).thenReturn(Optional.empty());

        final boolean actual = fileService.deleteTags("id", List.of("tag1", "tag2"));

        verifyNoMoreInteractions(fileRepository);
        assertFalse(actual);
    }

    @Test
    void deleteTags_shouldReturnFalse_whenNoTagsArePresentInFoundFile() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, null)));

        final boolean actual = fileService.deleteTags("id", List.of("tag1", "tag2"));

        assertFalse(actual);
    }

    @Test
    void deleteTags_shouldReturnFalse_whenNotAllTagsArePresentInFoundFile() {
        when(fileRepository.findById("id")).thenReturn(Optional.of(new File("id", "name", 0L, List.of("tag1"))));

        final boolean actual = fileService.deleteTags("id", List.of("tag1", "tag2"));

        assertFalse(actual);
    }

    @Test
    void deleteTags_shouldReturnTrueAndRemoveAllTags_whenAllTagsArePresentInFoundFile() {
        final File file = new File("id", "name", 0L, List.of("tag1", "tag2"));
        when(fileRepository.findById("id")).thenReturn(Optional.of(file));

        final boolean actual = fileService.deleteTags("id", List.of("tag1", "tag2"));

        assertTrue(actual);
        assertThat(file.getTags()).isEmpty();
    }

    @Test
    void deleteTags_shouldReturnTrueAndRemoveUnwantedTags_whenAllTagsArePresentInFoundFile() {
        final File file = new File("id", "name", 0L, List.of("tag1", "tag2", "tag3"));
        when(fileRepository.findById("id")).thenReturn(Optional.of(file));

        final boolean actual = fileService.deleteTags("id", List.of("tag1", "tag2"));

        assertTrue(actual);
        assertEquals(1, file.getTags().size());
        assertEquals("tag3", file.getTags().get(0));
    }
}
