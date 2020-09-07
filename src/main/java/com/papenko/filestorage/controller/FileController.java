package com.papenko.filestorage.controller;

import com.papenko.filestorage.dto.*;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.exception.FileOperation400Exception;
import com.papenko.filestorage.exception.FileOperation404Exception;
import com.papenko.filestorage.service.FileService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

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
    public ResponseEntity<ResponseEntityBody> upload(@Valid @RequestBody FileDto fileDto) {
        final File uploadedFile = fileService.uploadFile(fileDto.toFile());
        return ResponseEntity.ok(new Id(uploadedFile.getId()));
    }

    @GetMapping("{ID}")
    public ResponseEntity<File> get(@PathVariable(name = "ID") String id) {
        return ResponseEntity.ok(fileService.getById(id));
    }

    @DeleteMapping("{ID}")
    public ResponseEntity<ErrorMessage> delete(@PathVariable(name = "ID") String id) {
        fileService.delete(id);
        return ResponseEntity.ok(new ErrorMessage());
    }

    @PostMapping("{ID}/tags")
    public ResponseEntity<ErrorMessage> postTags(@PathVariable(name = "ID") String id,
                                                 @RequestBody Set<String> tags) {
        fileService.addTags(id, tags);
        return ResponseEntity.ok(new ErrorMessage());
    }

    @DeleteMapping("{ID}/tags")
    public ResponseEntity<ErrorMessage> deleteTags(@PathVariable(name = "ID") String id,
                                                   @RequestBody Set<String> tags) {
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleException(MethodArgumentNotValidException e) {
        final String error = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorMessage(error));
    }
}
