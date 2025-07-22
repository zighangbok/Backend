package com.backend.zighangbok.global.config;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris; // 콤마로 구분된 여러 URI도 가능

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient openSearchClient() {
        // 기본 인증 제공자 세팅
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        // URI 분리 (여러 개 있을 경우)
        String[] hosts = elasticsearchUris.split(",");
        HttpHost[] httpHosts = new HttpHost[hosts.length];

        for (int i = 0; i < hosts.length; i++) {
            // URI에서 프로토콜, 호스트, 포트 분리
            // 예시: https://hostname:port
            String uri = hosts[i].trim();
            String scheme = uri.startsWith("https") ? "https" : "http";
            String hostPort = uri.replaceFirst("^https?://", "");
            String host;
            int port;

            if (hostPort.contains(":")) {
                String[] hp = hostPort.split(":");
                host = hp[0];
                port = Integer.parseInt(hp[1]);
            } else {
                host = hostPort;
                port = scheme.equals("https") ? 443 : 80;
            }

            httpHosts[i] = new HttpHost(host, port, scheme);
        }

        return new RestHighLevelClient(
                RestClient.builder(httpHosts)
                        .setHttpClientConfigCallback(httpClientBuilder ->
                                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                                        .setKeepAliveStrategy((response, context) -> 60000) // 60초 유지
                                        .setDefaultIOReactorConfig(IOReactorConfig.custom()
                                                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                                                .build())
                        )
        );
    }
}