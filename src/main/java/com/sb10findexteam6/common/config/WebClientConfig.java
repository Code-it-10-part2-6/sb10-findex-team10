package com.sb10findexteam6.common.config;

import com.sb10findexteam6.common.config.properties.OpenApiProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final OpenApiProperties openApiProperties;

    @Bean
    public WebClient openApiWebClient() {
        // 타임아웃 설정을 위한 Netty HttpClient 객체 생성
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // Connection Timeout: 5초
            .responseTimeout(Duration.ofMillis(5000))           // Response Timeout: 5초
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))  // Read Timeout
                    .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)) // Write Timeout
            );

        // WebClient 빌드 및 Bean 등록
        return WebClient.builder()
            .baseUrl(openApiProperties.getBaseUrl()) //Base URL 주입
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader("Accept", "application/json") // 응답을 JSON으로 받도록 기본 헤더 설정
            .build();
    }
}