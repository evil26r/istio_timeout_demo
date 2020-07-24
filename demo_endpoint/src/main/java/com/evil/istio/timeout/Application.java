package com.evil.istio.timeout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RestController
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @RequestMapping(value = {"/", "/{path}"})
    public Mono<String> withDuration(@PathVariable(value = "path", required = false) String path,
                                     @RequestParam(value = "durationMs", defaultValue = "0") long durationMs) {
        log.info("Path: [{}], Receive request. Duration: [{}]", path, durationMs);
        return Mono.just("DurationMs: " + durationMs).delayElement(Duration.ofMillis(durationMs));
    }
}
