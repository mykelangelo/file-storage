package com.papenko.filestorage.service;

import com.papenko.filestorage.dto.FileValidityCheckReport;
import com.papenko.filestorage.dto.SlimFilePage;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.repository.FileCustomRepository;
import com.papenko.filestorage.repository.FileRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final FileRepository fileRepository;
    private final FileCustomRepository fileCustomRepository;

    public FileService(FileRepository fileRepository, FileCustomRepository fileCustomRepository) {
        this.fileRepository = fileRepository;
        this.fileCustomRepository = fileCustomRepository;
    }

    public File uploadFile(File file) {
        return fileRepository.save(file);
    }

    public FileValidityCheckReport isFileValid(File file) {
        if (Strings.isBlank(file.getName())) {
            return new FileValidityCheckReport(false, "file name is missing");
        }
        if (file.getSize() == null) {
            return new FileValidityCheckReport(false, "file size is missing");
        }
        if (file.getSize() < 0) {
            return new FileValidityCheckReport(false, "file size is negative");
        }
        return new FileValidityCheckReport(true, null);
    }

    public void delete(String id) {
        fileRepository.deleteById(id);
    }

    public boolean isPresentById(String id) {
        return fileRepository.existsById(id);
    }

    public void updateTags(String id, List<String> tags) {
        final Optional<File> fileOptional = fileRepository.findById(id);
        fileOptional.ifPresent(file -> file.setTags(tags));
    }

    /**
     * @param id   id of document ({@link File})
     * @param tags tags to delete
     * @return {@code true} in case we deleted tags successfully,
     * {@code false} in case document didn't contain all the tags for deletion
     * or if the file was not found (must not happen if you use service correctly)
     */
    public boolean deleteTags(String id, List<String> tags) {
        final Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            return false;
        }
        final File file = fileOptional.get();
        if (file.getTags() == null || !file.getTags().containsAll(tags)) {
            return false;
        }
        file.setTags(file.getTags().stream()
                .filter(tag -> !tags.contains(tag))
                .collect(Collectors.toList()));
        return true;
    }

    public SlimFilePage findPageByTagsAndName(List<String> tags, Pageable pageable, String name) {
        Page<File> found = fileCustomRepository.findAllByTagsContainingAllIn(tags, pageable, name);
        return new SlimFilePage(found.getTotalElements(), found.getContent());
    }
}
