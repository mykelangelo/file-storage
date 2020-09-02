package com.papenko.filestorage.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.papenko.filestorage.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
    @Value("#{systemEnvironment['ELASTIC_HOST_AND_PORT']}")
    private String elasticHostAndPort;

    @Value("#{systemEnvironment['SPRING_PROFILES_ACTIVE']}")
    private String profile;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        if ("production".equals(profile)) {
            return RestClients.create(ClientConfiguration.create(elasticHostAndPort)).rest();
        }
        return RestClients.create(ClientConfiguration.localhost()).rest();
    }
}
