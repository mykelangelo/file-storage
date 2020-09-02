package com.papenko.filestorage.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
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

    @Value("#{systemEnvironment['ELASTIC_USERNAME']}")
    private String elasticUsername;

    @Value("#{systemEnvironment['ELASTIC_PASSWORD']}")
    private String elasticPassword;

    @Value("#{systemEnvironment['ELASTIC_HOST']}")
    private String elasticHost;

    @Value("#{systemEnvironment['ELASTIC_PORT']}")
    private Integer elasticPort;

    @Value("#{systemEnvironment['ELASTIC_PROTOCOL']}")
    private String elasticProtocol;

    @Value("#{systemEnvironment['SPRING_PROFILES_ACTIVE']}")
    private String profile;

    @Override
    public RestHighLevelClient elasticsearchClient() {
        if ("production".equals(profile)) {
            LOGGER.info("Using production environment");
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(elasticUsername, elasticPassword);
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);

            HttpHost httpHost = new HttpHost(elasticHost, elasticPort, elasticProtocol);
            RestClientBuilder restClientBuilder = RestClient.builder(httpHost);
            restClientBuilder.setHttpClientConfigCallback(h -> h.setDefaultCredentialsProvider(credentialsProvider));

            return new RestHighLevelClient(restClientBuilder);
        }
        LOGGER.info("Using default local environment");
        return RestClients.create(ClientConfiguration.localhost()).rest();
    }
}
