package com.urlshortener.service;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.util.Base62Encoder;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class UrlShortenerService {
    @Autowired
    private UrlMappingRepository repository;

    @Autowired
    private Base62Encoder encoder;

    private static int SHORT_LEN = 10;

    // Counter for generating unique IDs
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    private static final Logger logger = LoggerFactory.getLogger(UrlShortenerService.class);

    /**
     * Shortens a full URL
     * @param fullUrl
     * @return The short URL
     */
    public String shortenUrl(String fullUrl) {
        // Validate URL
        if (!isValidUrl(fullUrl)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        // Check if short URL already exists, return it, if does not exist, generate it
        return repository.findByFullUrl(fullUrl)
                .map(UrlMapping::getShortUrl)
                .orElseGet(() -> {
                    String shortUrl = generateShortUrl();
                    UrlMapping mapping = new UrlMapping();
                    mapping.setFullUrl(fullUrl);
                    mapping.setShortUrl(shortUrl);
                    repository.save(mapping);
                    return shortUrl;
                });
    }

    /**
     * Retrieves a full URL from a short URL
     * @param shortUrl
     * @return The full URL
     */
    public String getFullUrl(String shortUrl) {
        return repository.findByShortUrl(shortUrl)
                .map(urlMapping -> {
                    repository.save(urlMapping);
                    return urlMapping.getFullUrl();
                })
                .orElseThrow(() -> {
                    logger.error("Short URL not found: {}", shortUrl);
                    return new IllegalArgumentException("Short URL not found");
                });
    }

    private String generateShortUrl() {
        String shortUrl = reduce(encoder.encode(counter.incrementAndGet()));
        // avoid duplication
        return repository.findByShortUrl(shortUrl).isPresent()
                ? generateShortUrl()
                : shortUrl;
    }

    String reduce(String input) {
        if (input == null) return null;
        return input.length() > SHORT_LEN ? input.substring(0, SHORT_LEN) : input;
    }

    private boolean isValidUrl(String url) {
        UrlValidator validator = new UrlValidator(new String[]{"http", "https"});
        return validator.isValid(url);
    }
}
