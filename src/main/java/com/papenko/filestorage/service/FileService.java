package com.papenko.filestorage.service;

import com.papenko.filestorage.dto.SlimFilePage;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.exception.FileOperation400Exception;
import com.papenko.filestorage.exception.FileOperation404Exception;
import com.papenko.filestorage.repository.FileCustomRepository;
import com.papenko.filestorage.repository.FileRepository;
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

    public void delete(String id) {
        if (!fileRepository.existsById(id)) {
            throw new FileOperation404Exception();
        }
        fileRepository.deleteById(id);
    }

    public void updateTags(String id, List<String> tags) {
        final Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            throw new FileOperation404Exception();
        }
        fileRepository.save(fileOptional.get().withTags(tags));
    }
    
    public void deleteTags(String id, List<String> tags) {
        final Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            throw new FileOperation404Exception();
        }
        final File file = fileOptional.get();
        if (!file.getTags().containsAll(tags)) {
            throw new FileOperation400Exception("tag not found on file");
        }
        final File withTags = file.withTags(file.getTags().stream()
                .filter(tag -> !tags.contains(tag))
                .collect(Collectors.toList()));
        fileRepository.save(withTags);
    }

    public SlimFilePage findPageByTagsAndName(List<String> tags, Pageable pageable, String name) {
        Page<File> found = fileCustomRepository.findAllByTagsContainingAllIn(tags, pageable, name);
        return new SlimFilePage(found.getTotalElements(), found.getContent());
    }
}
