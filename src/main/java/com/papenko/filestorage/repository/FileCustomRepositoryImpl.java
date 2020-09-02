package com.papenko.filestorage.repository;

import com.papenko.filestorage.entity.File;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
public class FileCustomRepositoryImpl implements FileCustomRepository {
    private final ElasticsearchOperations operations;

    public FileCustomRepositoryImpl(@Qualifier("elasticsearchOperations") ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Page<File> findAllByTagsContainingAllIn(List<String> tags, Pageable pageable, String name) {
        try (var closeableIterator = operations.searchForStream(getQueryBuilder(tags, name), File.class)) {
            return convertToPage(closeableIterator, pageable);
        }
    }

    Page<File> convertToPage(SearchHitsIterator<File> iterator, Pageable pageable) {
        for (int i = 0; i < pageable.getOffset() && iterator.hasNext(); i++) {
            iterator.next();
        }

        List<File> result = new ArrayList<>(pageable.getPageSize());

        for (int i = 0; i < pageable.getPageSize() && iterator.hasNext(); i++) {
            result.add(iterator.next().getContent());
        }

        return new PageImpl<>(result, pageable, result.size());
    }

    NativeSearchQuery getQueryBuilder(List<String> tags, String name) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = boolQuery();

        if (tags != null) {
            for (String tag : tags) {
                boolQueryBuilder.must(queryStringQuery(tag).field("tags"));
            }
        }

        if (name != null) {
            boolQueryBuilder.should(regexpQuery("name", ".*" + name + ".*"));
            boolQueryBuilder.should(queryStringQuery(name).field("name"));
        }

        searchQueryBuilder.withFilter(boolQueryBuilder);

        return searchQueryBuilder.build();
    }
}
