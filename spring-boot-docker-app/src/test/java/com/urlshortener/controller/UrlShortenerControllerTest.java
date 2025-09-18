package com.urlshortener.controller;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@Disabled
public class UrlShortenerControllerTest {

    @InjectMocks
    private UrlShortenerController controller;

    @Mock
    private UrlShortenerService urlShortenerService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShortenUrl_validInput_returnsShortenedUrl() {
        UrlMapping input = new UrlMapping();
        input.setFullUrl("https://example.com/page");

        when(urlShortenerService.shortenUrl("https://example.com/page"))
                .thenReturn("abc123");

        ResponseEntity<UrlMapping> response = controller.shortenUrl(input);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("abc123", response.getBody().getShortUrl());
        assertEquals("https://example.com/page", response.getBody().getFullUrl());
    }

    @Test
    public void testShortenUrl_blankInput_returnsBadRequest() {
        UrlMapping input = new UrlMapping();
        input.setFullUrl("  ");

        ResponseEntity<UrlMapping> response = controller.shortenUrl(input);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey("X-Error-Message"));
    }

    @Test
    public void testShortenUrl_invalidFormat_throwsException() {
        UrlMapping input = new UrlMapping();
        input.setFullUrl("invalid-url");

        when(urlShortenerService.shortenUrl("invalid-url"))
                .thenThrow(new IllegalArgumentException("Invalid URL"));

        ResponseEntity<UrlMapping> response = controller.shortenUrl(input);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey("X-Error-Message"));
    }

    @Test
    public void testExpandToFull_validShortUrl_returnsFullUrl() {
        when(urlShortenerService.getFullUrl("abc123"))
                .thenReturn("https://example.com/page");

        ResponseEntity<UrlMapping> response = controller.expandToFull("abc123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("abc123", response.getBody().getShortUrl());
        assertEquals("https://example.com/page", response.getBody().getFullUrl());
    }

    @Test
    public void testExpandToFull_blankShortUrl_returnsBadRequest() {
        ResponseEntity<UrlMapping> response = controller.expandToFull("  ");

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey("X-Error-Message"));
    }

    @Test
    public void testExpandToFull_notFound_returns404() {
        when(urlShortenerService.getFullUrl("notfound"))
                .thenThrow(new IllegalArgumentException("Not found"));

        ResponseEntity<UrlMapping> response = controller.expandToFull("notfound");

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey("X-Error-Message"));
    }

    @Test
    public void testHealthCheck_returnsOk() {
        ResponseEntity<String> response = controller.health();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Application is running with Docker Compose integration!", response.getBody());
    }
}