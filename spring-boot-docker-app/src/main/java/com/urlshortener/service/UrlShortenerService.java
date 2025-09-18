package com.urlshortener.service;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerService {

    private final UrlMappingRepository repository;


    private final Base62Encoder encoder;

    private static final int SHORT_LEN = 10;

    // Counter for generating unique IDs
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

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
                .map(UrlMapping::getFullUrl)
                .orElseThrow(() -> {
                    log.error("Short URL not found: {}", shortUrl);
                    return new IllegalArgumentException("Short URL not found");
                });
    }

    private String generateShortUrl() {
        String shortUrl;
        do {
            shortUrl = reduce(encoder.encode(counter.getAndAdd(1000)));
        } while (repository.existsByShortUrl(shortUrl));

        return shortUrl;
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
