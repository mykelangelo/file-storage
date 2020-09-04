package com.papenko.filestorage.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@TestConfiguration
@EnableElasticsearchRepositories(basePackages = "com.papenko.filestorage.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Override
    public RestHighLevelClient elasticsearchClient() {
        LOGGER.info("Using test environment");
        return new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9201, "http")));
    }
}
