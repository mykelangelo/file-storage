package com.papenko.filestorage.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FileCustomRepositoryImplTest {
    @InjectMocks
    @Spy
    private FileCustomRepositoryImpl fileCustomRepository;
    @Mock
    private ElasticsearchOperations operations;

    @Test
    void getQueryBuilder_shouldCreateEmptyNativeQueryBuilder_whenTagsListIsNullAndNameIsNull() {
        final NativeSearchQuery query = fileCustomRepository.getQueryBuilder(null, null);

        assertEquals(boolQuery(), query.getFilter());
    }

    @Test
    void getQueryBuilder_shouldCreateProperNativeQueryBuilder_whenTagsListIsNotNullAndNameIsNull() {
        final NativeSearchQuery query = fileCustomRepository.getQueryBuilder(List.of("tag1", "tag2"), null);

        assertEquals(boolQuery()
                        .must(queryStringQuery("tag1").field("tags"))
                        .must(queryStringQuery("tag2").field("tags")),
                query.getFilter());
    }

    @Test
    void getQueryBuilder_shouldCreateProperNativeQueryBuilder_whenTagsListIsNotNullAndNameIsNotNull() {
        final NativeSearchQuery query = fileCustomRepository.getQueryBuilder(List.of("tag1", "tag2"), "name");

        assertEquals(boolQuery()
                        .must(queryStringQuery("tag1").field("tags"))
                        .must(queryStringQuery("tag2").field("tags"))
                        .must(boolQuery()
                                .should(regexpQuery("name", ".*name.*"))
                                .should(queryStringQuery("name").field("name"))),
                query.getFilter());
    }

    @Test
    void getQueryBuilder_shouldCreateProperNativeQueryBuilder_whenTagsListIsNullAndNameIsNotNull() {
        final NativeSearchQuery query = fileCustomRepository.getQueryBuilder(null, "name");

        assertEquals(boolQuery()
                        .must(boolQuery()
                                .should(regexpQuery("name", ".*name.*"))
                                .should(queryStringQuery("name").field("name"))),
                query.getFilter());
    }
}
