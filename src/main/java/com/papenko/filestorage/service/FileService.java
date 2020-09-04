package com.papenko.filestorage.service;

import com.papenko.filestorage.dto.SlimFilePage;
import com.papenko.filestorage.entity.File;
import com.papenko.filestorage.exception.FileOperation400Exception;
import com.papenko.filestorage.exception.FileOperation404Exception;
import com.papenko.filestorage.properties.FileExtensionProperties;
import com.papenko.filestorage.repository.FileCustomRepository;
import com.papenko.filestorage.repository.FileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final FileExtensionProperties fileExtensionProperties;
    private final FileRepository fileRepository;
    private final FileCustomRepository fileCustomRepository;

    public FileService(FileExtensionProperties fileExtensionProperties,
                       FileRepository fileRepository,
                       FileCustomRepository fileCustomRepository) {
        this.fileExtensionProperties = fileExtensionProperties;
        this.fileRepository = fileRepository;
        this.fileCustomRepository = fileCustomRepository;
    }

    private static String substringAfterLastDot(String string) {
        int lastIndexOf = string.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return string.substring(lastIndexOf + 1);
    }

    public File uploadFile(File file) {
        return fileRepository.save(file.withTags(rectifyTags(file)));
    }

    Set<String> rectifyTags(File file) {
        Optional<String> firstTag = Optional.empty();
        if (file.getName() != null) {
            firstTag = defineFirstTagIfApplicable(file.getName());
        }
        Set<String> newTags;
        if (firstTag.isEmpty()) {
            newTags = file.getTags() == null ? Set.of() : file.getTags();
        } else {
            newTags = file.getTags() == null ?
                    new HashSet<>(1) :
                    new HashSet<>(file.getTags());
            newTags.add(firstTag.get());
        }
        return file.getTags() == null ?
                newTags :
                Set.copyOf(newTags.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet()));
    }

    private Optional<String> defineFirstTagIfApplicable(String name) {
        String extension = substringAfterLastDot(name).toLowerCase();
        if (extension.stripLeading().isEmpty()) {
            return Optional.empty();
        }
        for (var formatAndExtension : fileExtensionProperties.getExtensions().entrySet()) {
            if (formatAndExtension.getValue().contains(extension)) {
                return Optional.of(formatAndExtension.getKey().substring(2));
            }
        }
        return Optional.empty();
    }

    public void delete(String id) {
        if (!fileRepository.existsById(id)) {
            throw new FileOperation404Exception();
        }
        fileRepository.deleteById(id);
    }

    public void addTags(String id, Set<String> tags) {
        final Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            throw new FileOperation404Exception();
        }
        Set<String> newTags = new HashSet<>(fileOptional.get().getTags());
        newTags.addAll(tags);
        fileRepository.save(fileOptional.get().withTags(newTags));
    }

    public void deleteTags(String id, Collection<String> tags) {
        final Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            throw new FileOperation404Exception();
        }
        final File file = fileOptional.get();
        final Set<String> lowerCaseTags = tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (!file.getTags().containsAll(lowerCaseTags)) {
            throw new FileOperation400Exception("tag not found on file");
        }
        final File withTags = file.withTags(file.getTags().stream()
                .filter(tag -> !lowerCaseTags.contains(tag))
                .collect(Collectors.toSet()));
        fileRepository.save(withTags);
    }

    public SlimFilePage findPageByTagsAndName(List<String> tags, Pageable pageable, String name) {
        Page<File> found = fileCustomRepository.findAllByTagsContainingAllIn(tags, pageable, name);
        return new SlimFilePage(found.getTotalElements(), found.getContent());
    }
}
