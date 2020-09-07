package com.papenko.filestorage.repository;

import com.papenko.filestorage.entity.File;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Repository
public class FileCustomRepositoryImpl implements FileCustomRepository {
    private final ElasticsearchOperations operations;

    public FileCustomRepositoryImpl(@Qualifier("elasticsearchOperations") ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Page<File> findAllByTagsContainingAllIn(List<String> tags, Pageable pageable, String name) {
        SearchHits<File> searchHits
                = operations.search(getQueryBuilder(tags, name).withPageable(pageable).build(), File.class);
        return new PageImpl<>(searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList()), pageable, searchHits.getTotalHits());
    }

    NativeSearchQueryBuilder getQueryBuilder(List<String> tags, String name) {
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

        return searchQueryBuilder;
    }
}
