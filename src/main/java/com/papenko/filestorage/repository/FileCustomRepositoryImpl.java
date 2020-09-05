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
        int total = 0;

        for (int i = 0; i < pageable.getOffset() && iterator.hasNext(); i++) {
            iterator.next();
            total++;
        }

        List<File> result = new ArrayList<>(pageable.getPageSize());

        for (int i = 0; iterator.hasNext(); i++) {
            if (i < pageable.getPageSize()) {
                result.add(iterator.next().getContent());
            } else {
                iterator.next();
            }
            total++;
        }

        return new PageImpl<>(result, pageable, total);
    }

    NativeSearchQuery getQueryBuilder(List<String> tags, String name) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = boolQuery();

        if (tags != null) {
            for (String tag : tags) {
                boolQueryBuilder.must(queryStringQuery(tag).field("tags"));
            }
        }

        BoolQueryBuilder shouldMatchName = boolQuery();
        if (name != null) {
            shouldMatchName.should(regexpQuery("name", ".*" + name + ".*"));
            shouldMatchName.should(queryStringQuery(name).field("name"));
            boolQueryBuilder.must(shouldMatchName);
        }

        searchQueryBuilder.withFilter(boolQueryBuilder);

        return searchQueryBuilder.build();
    }
}
