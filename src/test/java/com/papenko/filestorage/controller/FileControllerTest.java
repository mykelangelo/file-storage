package com.papenko.filestorage.controller;

import com.papenko.filestorage.dto.*;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {
    @InjectMocks
    private FileController fileController;
    @Mock
    private FileService fileService;

    @Test
    void upload_shouldReturnBadRequestAndErrorMessage_whenNameIsMissing() {
        File file = new File(null, null, 0L, null);
        when(fileService.isFileValid(file)).thenReturn(new FileValidityCheckReport(false, "file name is missing"));

        final ResponseEntity<ResponseEntityBody> responseEntity = fileController.upload(file);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertTrue(responseEntity.getBody() instanceof ErrorMessage);
        assertEquals("file name is missing", ((ErrorMessage) responseEntity.getBody()).getError());
        assertFalse(((ErrorMessage) responseEntity.getBody()).getSuccess());
    }

    @Test
    void upload_shouldReturnOkAndId_whenFileIsValid() {
        File newFile = new File(null, "name", 1L, null);
        File fileWithId = new File("id", "name", 1L, null);
        when(fileService.isFileValid(newFile)).thenReturn(new FileValidityCheckReport(true, null));
        doReturn(fileWithId).when(fileService).uploadFile(newFile);

        final ResponseEntity<ResponseEntityBody> responseEntity = fileController.upload(newFile);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertTrue(responseEntity.getBody() instanceof Id);
        assertThat(((Id) responseEntity.getBody()).getId()).isNotBlank();
    }

    @Test
    void delete_shouldReturnNotFoundAndErrorMessage_whenNoFileWithSuchIdIsFound() {
        doReturn(false).when(fileService).isPresentById("id0");

        final ResponseEntity<SuccessStatus> responseEntity = fileController.delete("id0");

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertTrue(responseEntity.getBody() instanceof ErrorMessage);
        assertEquals("file not found", ((ErrorMessage) responseEntity.getBody()).getError());
        assertFalse(responseEntity.getBody().getSuccess());
    }

    @Test
    void delete_shouldReturnOk_whenFileWithSuchIdIsFound() {
        doReturn(true).when(fileService).isPresentById("id0");
        doNothing().when(fileService).delete("id0");

        final ResponseEntity<SuccessStatus> responseEntity = fileController.delete("id0");

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertFalse(responseEntity.getBody() instanceof ErrorMessage);
        assertTrue(responseEntity.getBody().getSuccess());
    }

    @Test
    void postTags_shouldReturnNotFoundAndErrorMessage_whenNoFileWithSuchIdIsFound() {
        doReturn(false).when(fileService).isPresentById("id0");

        ResponseEntity<SuccessStatus> responseEntity = fileController.postTags("id0", List.of("tag1", "tag2"));

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertTrue(responseEntity.getBody() instanceof ErrorMessage);
        assertEquals("file not found", ((ErrorMessage) responseEntity.getBody()).getError());
        assertFalse(responseEntity.getBody().getSuccess());
    }

    @Test
    void postTags_shouldReturnOk_whenFileWithSuchIdIsFound() {
        doReturn(true).when(fileService).isPresentById("id0");
        doNothing().when(fileService).updateTags("id0", List.of("tag1", "tag2"));

        ResponseEntity<SuccessStatus> responseEntity = fileController.postTags("id0", List.of("tag1", "tag2"));

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertFalse(responseEntity.getBody() instanceof ErrorMessage);
        assertTrue(responseEntity.getBody().getSuccess());
    }

    @Test
    void deleteTags_shouldReturnOk_whenFileWithSuchIdAndTagsIsFound() {
        doReturn(true).when(fileService).isPresentById("id0");
        doReturn(true).when(fileService).deleteTags("id0", List.of("tag1", "tag2"));

        ResponseEntity<SuccessStatus> responseEntity = fileController.deleteTags("id0", List.of("tag1", "tag2"));

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertFalse(responseEntity.getBody() instanceof ErrorMessage);
        assertTrue(responseEntity.getBody().getSuccess());
    }

    @Test
    void deleteTags_shouldReturnNotFound_whenNoFileWithSuchIdIsFound() {
        doReturn(false).when(fileService).isPresentById("id0");

        ResponseEntity<SuccessStatus> responseEntity = fileController.deleteTags("id0", List.of("tag1", "tag2"));

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertTrue(responseEntity.getBody() instanceof ErrorMessage);
        assertFalse(responseEntity.getBody().getSuccess());
        assertEquals("file not found", ((ErrorMessage) responseEntity.getBody()).getError());
    }

    @Test
    void deleteTags_shouldReturnBadRequest_whenFileWithSuchIdDoesNotContainAllTags() {
        doReturn(true).when(fileService).isPresentById("id0");
        doReturn(false).when(fileService).deleteTags("id0", List.of("tag1", "tag2"));

        ResponseEntity<SuccessStatus> responseEntity = fileController.deleteTags("id0", List.of("tag1", "tag2"));

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertTrue(responseEntity.getBody() instanceof ErrorMessage);
        assertFalse(responseEntity.getBody().getSuccess());
        assertEquals("tag not found on file", ((ErrorMessage) responseEntity.getBody()).getError());
    }

    @Test
    void findByTags_shouldReturnOkAndEmptyPage_whenNoFilesWerePresentInDb() {
        doReturn(Page.empty()).when(fileService).findPageByTags(null, PageRequest.of(0, 10));

        final ResponseEntity<Page<File>> responseEntity = fileController.findByTags(null, 0, 10);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertTrue(responseEntity.getBody().isEmpty());
    }

    @Test
    void findByTags_shouldReturnOkAndPageWithTwoFiles_whenThoseAreTheOnlyFilesInDb() {
        final File file0 = new File("id0", "name0", 0L, null);
        final File file1 = new File("id1", "name1", 1L, null);
        final PageImpl<File> files = new PageImpl<>(List.of(file0, file1));
        doReturn(files).when(fileService).findPageByTags(null, PageRequest.of(0, 10));

        final ResponseEntity<Page<File>> responseEntity = fileController.findByTags(null, 0, 10);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertFalse(responseEntity.getBody().isEmpty());
        assertEquals(2, responseEntity.getBody().getTotalElements());
        assertEquals(List.of(file0, file1), responseEntity.getBody().get().collect(Collectors.toList()));
    }

    @Test
    void findByTags_shouldReturnOkAndPageWithOneFiles_whenThisIsTheOnlyFileWithSuchTagsInDb() {
        final File file0 = new File("id0", "name0", 0L, List.of("super", "duper"));
        final PageImpl<File> files = new PageImpl<>(List.of(file0));
        doReturn(files).when(fileService).findPageByTags(List.of("super", "duper"), PageRequest.of(0, 10));

        final ResponseEntity<Page<File>> responseEntity = fileController.findByTags(List.of("super", "duper"), 0, 10);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertFalse(responseEntity.getBody().isEmpty());
        assertEquals(1, responseEntity.getBody().getTotalElements());
        assertEquals(List.of(file0), responseEntity.getBody().get().collect(Collectors.toList()));
    }
}
