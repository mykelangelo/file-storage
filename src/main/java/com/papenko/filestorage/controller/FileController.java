package com.papenko.filestorage.controller;

import com.papenko.filestorage.dto.ErrorMessage;
import com.papenko.filestorage.dto.Id;
import com.papenko.filestorage.dto.ResponseEntityBody;
import com.papenko.filestorage.dto.SlimFilePage;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.exception.FileOperation400Exception;
import com.papenko.filestorage.exception.FileOperation404Exception;
import com.papenko.filestorage.service.FileService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<SlimFilePage> findByTagsAndName(@RequestParam(required = false) List<String> tags,
                                                          @RequestParam(required = false) String q,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok().body(fileService.findPageByTagsAndName(tags, PageRequest.of(page, size), q));
    }

    @PostMapping
    public ResponseEntity<ResponseEntityBody> upload(@RequestBody File file) {
        final File uploadedFile = fileService.uploadFile(file);
        return ResponseEntity.ok(new Id(uploadedFile.getId()));
    }

    @DeleteMapping("{ID}")
    public ResponseEntity<ErrorMessage> delete(@PathVariable(name = "ID") String id) {
        fileService.delete(id);
        return ResponseEntity.ok(new ErrorMessage());
    }

    @PostMapping("{ID}/tags")
    public ResponseEntity<ErrorMessage> postTags(@PathVariable(name = "ID") String id,
                                                 @RequestBody List<String> tags) {
        fileService.updateTags(id, tags);
        return ResponseEntity.ok(new ErrorMessage());
    }

    @DeleteMapping("{ID}/tags")
    public ResponseEntity<ErrorMessage> deleteTags(@PathVariable(name = "ID") String id,
                                                   @RequestBody List<String> tags) {
        fileService.deleteTags(id, tags);
        return ResponseEntity.ok(new ErrorMessage());
    }

    @ExceptionHandler(FileOperation400Exception.class)
    public ResponseEntity<ErrorMessage> handleException(FileOperation400Exception e) {
        return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(FileOperation404Exception.class)
    public ResponseEntity<ErrorMessage> handleException(FileOperation404Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
    }
}
