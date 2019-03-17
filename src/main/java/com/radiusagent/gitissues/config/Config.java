package com.radiusagent.gitissues.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * @author Parisana
 */
@Configuration
@Slf4j
public class Config {

    @Bean
    public WebClient webClient(){
        return WebClient.builder().filter(logRequest()).baseUrl("https://api.github.com").build();

    }

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(new Function<ClientRequest, Mono<ClientRequest>>() {
            @Override
            public Mono<ClientRequest> apply(ClientRequest clientRequest) {
                log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.attributes().forEach((k, v)-> log.info("key: " + k + " : " + v));
                clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
                return Mono.just(clientRequest);
            }
        });
    }

}
