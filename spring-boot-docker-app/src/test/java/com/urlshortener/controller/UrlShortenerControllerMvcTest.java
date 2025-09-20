package com.urlshortener.controller;

import com.urlshortener.entity.UrlMapping;
import com.urlshortener.service.UrlShortenerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(controllers = UrlShortenerController.class)
@ContextConfiguration(classes = UrlShortenerController.class)
@DisplayName("UrlShortenerController Tests")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UrlShortenerControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @Autowired
    private ObjectMapper objectMapper;

    private UrlMapping validUrlMapping;
    private final String BASE_URL = "/api/url";

    @BeforeEach
    void setUp() {
        validUrlMapping = new UrlMapping();
        validUrlMapping.setFullUrl("https://www.example.com");
    }

    @Test
    @DisplayName("POST /shorten - Should successfully shorten valid URL")
    void shortenUrl_ValidUrl_ReturnsSuccess() throws Exception {
        // Given
        String expectedShortUrl = "abc123";
        when(urlShortenerService.shortenUrl(anyString())).thenReturn(expectedShortUrl);

        // Add breakpoint here - this should hit first
        System.out.println("DEBUG: About to call controller via MockMvc");

        // When & Then
        mockMvc.perform(post(BASE_URL + "/shorten")
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUrlMapping)))
                .andDo(print()) // This will print the request/response details
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fullUrl", is("https://www.example.com")))
                .andExpect(jsonPath("$.shortUrl", is(expectedShortUrl)))
                .andExpect(jsonPath("$.error").doesNotExist());

        System.out.println("DEBUG: MockMvc call completed");
        verify(urlShortenerService, times(1)).shortenUrl("https://www.example.com");
    }

    @Test
    @DisplayName("POST /shorten - Should return error for empty URL")
    void shortenUrl_EmptyUrl_ReturnsBadRequest() throws Exception {
        // Given
        UrlMapping emptyUrlMapping = new UrlMapping();
        emptyUrlMapping.setFullUrl("");

        // When & Then
        mockMvc.perform(post(BASE_URL + "/shorten")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyUrlMapping)))
                .andExpect(status().isBadRequest());
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.error", is("Full URL must not be empty")))
//                .andExpect(header().string("X-Error-Message", "Full URL must not be empty"));

        verify(urlShortenerService, never()).shortenUrl(anyString());
    }

    @Test
    @DisplayName("POST /shorten - Should return error for null URL")
    void shortenUrl_NullUrl_ReturnsBadRequest() throws Exception {
        // Given
        UrlMapping nullUrlMapping = new UrlMapping();
        nullUrlMapping.setFullUrl(null);

        // When & Then
        mockMvc.perform(post(BASE_URL + "/shorten")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullUrlMapping)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Full URL must not be empty")))
                .andExpect(header().string("X-Error-Message", "Full URL must not be empty"));

        verify(urlShortenerService, never()).shortenUrl(anyString());
    }

    @Test
    @DisplayName("POST /shorten - Should return error for blank URL")
    void shortenUrl_BlankUrl_ReturnsBadRequest() throws Exception {
        // Given
        UrlMapping blankUrlMapping = new UrlMapping();
        blankUrlMapping.setFullUrl("   ");

        // When & Then
        mockMvc.perform(post(BASE_URL + "/shorten")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankUrlMapping)))
                .andExpect(status().isBadRequest());
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.error", is("Full URL must not be empty")))
//                .andExpect(header().string("X-Error-Message", "Full URL must not be empty"));

        verify(urlShortenerService, never()).shortenUrl(anyString());
    }

    @Test
    @DisplayName("POST /shorten - Should handle invalid URL format from service")
    void shortenUrl_InvalidUrlFormat_ReturnsBadRequest() throws Exception {
        // Given
        when(urlShortenerService.shortenUrl(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid URL"));

        // When & Then
        mockMvc.perform(post(BASE_URL + "/shorten")
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUrlMapping)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Invalid URL format")))
                .andExpect(header().string("X-Error-Message", "Invalid URL format"));

        verify(urlShortenerService, times(1)).shortenUrl("https://www.example.com");
    }

    @Test
    @DisplayName("GET /expand/{shortUrl} - Should successfully expand valid short URL")
    void expandToFull_ValidShortUrl_ReturnsSuccess() throws Exception {
        // Given
        String shortUrl = "abc123";
        String expectedFullUrl = "https://www.example.com";
        when(urlShortenerService.getFullUrl(shortUrl)).thenReturn(expectedFullUrl);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/expand/{shortUrl}", shortUrl)
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.shortUrl", is(shortUrl)))
                .andExpect(jsonPath("$.fullUrl", is(expectedFullUrl)))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(urlShortenerService, times(1)).getFullUrl(shortUrl);
    }

    @Test
    @DisplayName("GET /expand/{shortUrl} - Should return error for empty short URL")
    void expandToFull_EmptyShortUrl_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + "/expand/{shortUrl}", "")
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser")))
                .andExpect(status().isNotFound()); // Spring returns 404 for empty path variable

        verify(urlShortenerService, never()).getFullUrl(anyString());
    }

    @Test
    @DisplayName("GET /expand/{shortUrl} - Should return error for blank short URL")
    void expandToFull_BlankShortUrl_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + "/expand/{shortUrl}", "   ")
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Short URL must not be null or blank")))
                .andExpect(header().string("X-Error-Message", "Short URL must not be null or blank"));

        verify(urlShortenerService, never()).getFullUrl(anyString());
    }

    @Test
    @DisplayName("GET /expand/{shortUrl} - Should return error when short URL not found")
    void expandToFull_ShortUrlNotFound_ReturnsBadRequest() throws Exception {
        // Given
        String shortUrl = "notfound123";
        when(urlShortenerService.getFullUrl(shortUrl))
                .thenThrow(new IllegalArgumentException("Short URL not found"));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/expand/{shortUrl}", shortUrl)
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Short URL not found")))
                .andExpect(header().string("X-Error-Message", "Short URL not found"));

        verify(urlShortenerService, times(1)).getFullUrl(shortUrl);
    }

    @Test
    @DisplayName("GET /health - Should return health status")
    void health_ReturnsHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get(BASE_URL + "/health")
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Application is running with Docker Compose integration!"));
    }

    @Test
    @DisplayName("POST /shorten - Should handle malformed JSON")
    void shortenUrl_MalformedJson_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post(BASE_URL + "/shorten")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed json"))
                .andExpect(status().isBadRequest());

        verify(urlShortenerService, never()).shortenUrl(anyString());
    }

    @Test
    @DisplayName("Should handle CORS preflight request")
    void handleCorsPreflightRequest() throws Exception {
        // When & Then
        mockMvc.perform(options(BASE_URL + "/shorten")
                        .with(csrf()) // Add CSRF token
                        .with(user("testuser"))
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    @DisplayName("Should verify CORS headers are present")
    void verifyCorsHeaders() throws Exception {
        // Given
        when(urlShortenerService.shortenUrl(anyString())).thenReturn("abc123");

        // When & Then
        mockMvc.perform(post(BASE_URL + "/shorten")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUrlMapping))
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }
}