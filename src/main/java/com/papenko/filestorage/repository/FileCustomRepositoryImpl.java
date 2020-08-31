package com.papenko.filestorage.repository;

import com.papenko.filestorage.entity.File;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;

@Repository
public class FileCustomRepositoryImpl implements FileCustomRepository {
    private final ElasticsearchRestTemplate esTemplate;

    public FileCustomRepositoryImpl(ElasticsearchRestTemplate esTemplate) {
        this.esTemplate = esTemplate;
    }


    @Override
    public Page<File> findAllByTagsContainingAllIn(List<String> tags, Pageable pageable) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        searchQueryBuilder.withFilter(QueryBuilders.matchAllQuery());
        if (tags != null) {
            for (String tag : tags) {
                searchQueryBuilder.withFilter(regexpQuery("tags", ".*" + tag + ".*"));
            }
        }

        final List<SearchHit<File>> searchHits = esTemplate.search(searchQueryBuilder.build(), File.class).toList();

        int end = (pageable.getOffset() + pageable.getPageSize()) > searchHits.size() ?
                searchHits.size() : (int) (pageable.getOffset() + pageable.getPageSize());

        return new PageImpl<>(searchHits.subList((int) pageable.getOffset(), end).stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList()), pageable, searchHits.size());
    }
}
