package com.evil.istio.timeout.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RestControllers {

    @Value("${endpoint.host}")
    private String endpointHost;

    private final WebClient webClient;

    @RequestMapping
    public Mono<String> withDuration(@RequestParam(value = "durationMsIntermediary", defaultValue = "0") long durationMsIntermediary) {
        log.info("Receive request. DurationMs: [{}]", durationMsIntermediary);
        return Mono.just("Duration ms: " + durationMsIntermediary)
                .delayElement(Duration.ofMillis(durationMsIntermediary));
    }

    @RequestMapping("chain")
    public Mono<String> withDurationToIntermediary(@RequestParam(value = "path", defaultValue = "/") String path,
                                                   @RequestParam(value = "durationMs", defaultValue = "0") long durationMs,
                                                   @RequestParam(value = "durationMsIntermediary", defaultValue = "0") long durationMsIntermediary) {
        log.info("Receive request. DurationMs: [{}], DurationMsIntermediary: [{}]. Path: [{}]", durationMs, durationMsIntermediary, path);
        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path(path)
                                .queryParam("durationMs", String.valueOf(durationMs))
                                .queryParam("Host", endpointHost)
                                .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(throwable -> log.warn("Error Request!", throwable))
                .delayElement(Duration.ofMillis(durationMsIntermediary))
                .map(response -> "DurationMsIntermediary: " + durationMsIntermediary + " " + response);
    }

}
