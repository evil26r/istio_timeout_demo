package com.evil.istio.timeout.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RestControllers {

    @Value("${endpoint.host}")
    private String endpointHost;

    private final WebClient webClient;

    private double coefficient = 1;

    @RequestMapping
    public Mono<String> withDuration(@RequestParam(value = "durationMsIntermediary", defaultValue = "0") long durationMsIntermediary,
                                     @RequestHeader Map<String, String> headers) {
        log.info("[{}] Receive request. DurationMs: [{}]", headers.get("x-b3-spanid"), durationMsIntermediary);
        log.info("Headers: [{}]", headers);
        return Mono.just("Duration ms: " + durationMsIntermediary)
                .delayElement(Duration.ofMillis((long) (durationMsIntermediary * coefficient)));
    }

    @RequestMapping("chain")
    public Mono<String> withDurationToIntermediary(@RequestParam(value = "path", defaultValue = "/") String path,
                                                   @RequestParam(value = "durationMs", defaultValue = "0") long durationMs,
                                                   @RequestParam(value = "durationMsIntermediary", defaultValue = "0") long durationMsIntermediary,
                                                   @RequestHeader Map<String, String> headers) {
        log.info("[{}] Receive request. DurationMs: [{}], DurationMsIntermediary: [{}]. Path: [{}]", headers.get("x-b3-spanid"), durationMs, durationMsIntermediary, path);
        log.info("Headers: [{}]", headers);
        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path(path)
                                .queryParam("durationMs", String.valueOf(durationMs))
                                .build())
                .header("Host", endpointHost)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(throwable -> log.warn("Error Request!", throwable))
                .delayElement(Duration.ofMillis((long) (durationMsIntermediary * coefficient)))
                .map(response -> "DurationMsIntermediary: " + durationMsIntermediary + " " + response);
    }

    @RequestMapping("coefficient/{coefficient}")
    public Mono<Double> setCoefficient(@PathVariable Double coefficient) {
        log.info("Incoming coefficient: [{}]", coefficient);
        this.coefficient = coefficient;
        return Mono.just(this.coefficient);
    }

    @RequestMapping("coefficient")
    public Mono<Double> getCoefficient() {
        return Mono.just(coefficient);
    }
}
