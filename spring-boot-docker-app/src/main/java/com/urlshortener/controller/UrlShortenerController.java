package com.urlshortener.controller;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlMapping> shortenUrl(@Valid @RequestBody UrlMapping urlMapping) {
        String fullUrl = urlMapping.getFullUrl();

        if ( StringUtils.isBlank(fullUrl)) {
            return getErrorResponses("Full URL must not be empty");
        }

        try {
            urlMapping.setShortUrl(urlShortenerService.shortenUrl(fullUrl));
            return ResponseEntity.ok(urlMapping);
        } catch (IllegalArgumentException e) {
            return getErrorResponses("Invalid URL format");
        }
    }

    @GetMapping("/expand/{shortUrl}")
    public ResponseEntity<UrlMapping> expandToFull(@PathVariable String shortUrl) {
        if (StringUtils.isBlank(shortUrl)) {
            return getErrorResponses("Short URL must not be null or blank");
        }

        try {
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setShortUrl(shortUrl);
            urlMapping.setFullUrl(urlShortenerService.getFullUrl(shortUrl));
            return ResponseEntity.ok(urlMapping);
        } catch (IllegalArgumentException e) {
            return getErrorResponses("Short URL not found");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check requested");
        return ResponseEntity.ok("Application is running with Docker Compose integration!");
    }

    private ResponseEntity<UrlMapping> getErrorResponses(String errorMessage) {
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setError(errorMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Error-Message", errorMessage);
        log.error(errorMessage);
        return ResponseEntity.badRequest().headers(headers).body(urlMapping);
    }
}
