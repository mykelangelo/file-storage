package com.papenko.filestorage.controller;

import com.papenko.filestorage.dto.*;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.service.FileService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<ResponseEntityBody> upload(@RequestBody File file) {
        FileValidityCheckReport fileValidityCheckReport = fileService.isFileValid(file);
        if (!fileValidityCheckReport.isValid()) {
            return ResponseEntity.badRequest().body(new ErrorMessage(false, fileValidityCheckReport.getErrorMessage()));
        }
        final File uploadedFile = fileService.uploadFile(file);
        return ResponseEntity.ok(new Id(uploadedFile.getId()));
    }

    @DeleteMapping("{ID}")
    public ResponseEntity<SuccessStatus> delete(@PathVariable(name = "ID") String id) {
        if (fileService.isPresentById(id)) {
            fileService.delete(id);
            return ResponseEntity.ok(new SuccessStatus(true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(false, "file not found"));
    }

    @PostMapping("{ID}/tags")
    public ResponseEntity<SuccessStatus> postTags(@PathVariable(name = "ID") String id,
                                                       @RequestBody List<String> tags) {
        if (fileService.isPresentById(id)) {
            fileService.updateTags(id, tags);
            return ResponseEntity.ok(new SuccessStatus(true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(false, "file not found"));
    }

    @DeleteMapping("{ID}/tags")
    public ResponseEntity<SuccessStatus> deleteTags(@PathVariable(name = "ID") String id,
                                                       @RequestBody List<String> tags) {
        if (fileService.isPresentById(id)) {
            return fileService.deleteTags(id, tags) ?
                    ResponseEntity.ok(new SuccessStatus(true)) :
                    ResponseEntity.badRequest().body(new ErrorMessage(false, "tag not found on file"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(false, "file not found"));
    }

    @GetMapping
    public ResponseEntity<SlimFilePage> findByTags(@RequestParam(required = false) List<String> tags,
                                                   @RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok().body(fileService.findPageByTags(tags, PageRequest.of(page, size)));
    }
}
