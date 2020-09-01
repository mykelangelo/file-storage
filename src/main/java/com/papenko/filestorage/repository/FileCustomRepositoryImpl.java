package com.papenko.filestorage.repository;

import com.papenko.filestorage.entity.File;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@Repository
public class FileCustomRepositoryImpl implements FileCustomRepository {
    private final ElasticsearchOperations operations;

    public FileCustomRepositoryImpl(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Page<File> findAllByTagsContainingAllIn(List<String> tags, Pageable pageable) {
        return convertToPage(operations.search(getQueryBuilder(tags), File.class), pageable);
    }

    Page<File> convertToPage(SearchHits<File> search, Pageable pageable) {
        Iterator<SearchHit<File>> iterator = search.iterator();

        for (int i = 0; i < pageable.getOffset() && iterator.hasNext(); i++) {
            iterator.next();
        }

        List<File> result = new ArrayList<>(pageable.getPageSize());

        for (int i = 0; i < pageable.getPageSize() && iterator.hasNext(); i++) {
            result.add(iterator.next().getContent());
        }

        return new PageImpl<>(result, pageable, result.size());
    }

    NativeSearchQuery getQueryBuilder(List<String> tags) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = boolQuery();

        if (tags != null) {
            for (String tag : tags) {
                boolQueryBuilder.must(termQuery("tags", tag));
            }
        }

        searchQueryBuilder.withFilter(boolQueryBuilder);

        return searchQueryBuilder.build();
    }
}
