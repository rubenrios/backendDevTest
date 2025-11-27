package com.rubenrbr.products.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

  @Value("${external.api.product.base-url}")
  private String baseUrl;

  @Value("${external.api.product.timeout:5}")
  private int timeout;

  @Bean
  public WebClient productApiWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .clientConnector(
            new ReactorClientHttpConnector(
                HttpClient.create().responseTimeout(Duration.ofSeconds(timeout))))
        .build();
  }
}
