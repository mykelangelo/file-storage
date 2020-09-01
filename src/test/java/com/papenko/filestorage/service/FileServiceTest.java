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
        final List<String> tags = List.of("tag1", "tag2", "tag3");

        fileService.updateTags("id", tags);

        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(new File("id", "name", 0L, null).withTags(tags)));

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
        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(file.withTags(List.of())));
    }

    @Test
    void deleteTags_shouldReturnTrueAndRemoveUnwantedTags_whenAllTagsArePresentInFoundFile() {
        final File file = new File("id", "name", 0L, List.of("tag1", "tag2", "tag3"));
        when(fileRepository.findById("id")).thenReturn(Optional.of(file));

        final boolean actual = fileService.deleteTags("id", List.of("tag1", "tag2"));

        assertTrue(actual);
        verify(fileRepository).findById("id");
        verify(fileRepository).save(eq(file.withTags(List.of("tag3"))));
    }
}
