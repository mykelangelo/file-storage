package com.papenko.filestorage.repository;

import com.papenko.filestorage.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface FileCustomRepository {
    Page<File> findAllByTagsContainingAllIn(List<String> tags, Pageable pageable, String name);
}
