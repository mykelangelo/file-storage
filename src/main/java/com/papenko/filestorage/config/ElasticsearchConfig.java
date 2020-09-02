package com.papenko.filestorage.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.papenko.filestorage.repository")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfig.class);
    
    @Value("#{systemEnvironment['ELASTIC_HOST_AND_PORT']}")
    private String elasticHostAndPort;

    @Value("#{systemEnvironment['SPRING_PROFILES_ACTIVE']}")
    private String profile;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        if ("production".equals(profile)) {
            LOGGER.info("Using production environment");
            return RestClients.create(ClientConfiguration.create(elasticHostAndPort)).rest();
        }
        LOGGER.info("Using default local environment");
        return RestClients.create(ClientConfiguration.localhost()).rest();
    }
}
