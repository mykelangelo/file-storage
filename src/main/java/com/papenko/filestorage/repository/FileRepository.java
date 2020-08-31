package com.papenko.filestorage.repository;

import com.papenko.filestorage.entity.File;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends ElasticsearchRepository<File, String> {
}
