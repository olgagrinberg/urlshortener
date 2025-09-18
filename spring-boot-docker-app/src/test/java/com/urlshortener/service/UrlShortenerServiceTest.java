package com.urlshortener.service;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.util.Base62Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@Disabled
public class UrlShortenerServiceTest {

    @InjectMocks
    private UrlShortenerService service;

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private Base62Encoder encoder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShortenUrl_validNewUrl_generatesAndSavesShortUrl() {
        String fullUrl = "https://example.com/page";
        String encoded = "abc123xyz";
        String reduced = "abc123xyz";

        when(repository.findByFullUrl(fullUrl)).thenReturn(Optional.empty());
        when(encoder.encode(anyLong())).thenReturn(encoded);
        when(repository.findByShortUrl(reduced)).thenReturn(Optional.empty());

        String result = service.shortenUrl(fullUrl);

        assertEquals(reduced, result);
        verify(repository).save(any(UrlMapping.class));
    }

    @Test
    public void testShortenUrl_existingUrl_returnsExistingShortUrl() {
        String fullUrl = "https://example.com/page";
        UrlMapping existing = new UrlMapping();
        existing.setFullUrl(fullUrl);
        existing.setShortUrl("abc123");

        when(repository.findByFullUrl(fullUrl)).thenReturn(Optional.of(existing));

        String result = service.shortenUrl(fullUrl);

        assertEquals("abc123", result);
        verify(repository, never()).save(any());
    }

    @Test
    public void testShortenUrl_invalidUrl_throwsException() {
        String invalidUrl = "htp:/bad";

        assertThrows(IllegalArgumentException.class, () -> service.shortenUrl(invalidUrl));
        verify(repository, never()).save(any());
    }

    @Test
    public void testGetFullUrl_validShortUrl_returnsFullUrl() {
        String shortUrl = "abc123";
        UrlMapping mapping = new UrlMapping();
        mapping.setShortUrl(shortUrl);
        mapping.setFullUrl("https://example.com/page");

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(mapping));

        String result = service.getFullUrl(shortUrl);

        assertEquals("https://example.com/page", result);
    }

    @Test
    public void testGetFullUrl_notFound_throwsException() {
        String shortUrl = "notfound";

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getFullUrl(shortUrl));
    }

    @Test
    public void testReduce_trimsLongString() {
        String input = "abcdefghijklmnopqrstuvwxyz";
        String result = service.reduce(input);
        assertEquals(10, result.length());
        assertEquals("abcdefghij", result);
    }

    @Test
    public void testReduce_shortString_returnsOriginal() {
        String input = "short";
        String result = service.reduce(input);
        assertEquals("short", result);
    }

    @Test
    public void testReduce_nullInput_returnsNull() {
        assertNull(service.reduce(null));
    }
}