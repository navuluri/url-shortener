package com.url.shortener;

import com.url.shortener.api.ShortenerApi;
import com.url.shortener.models.ShortenerRequest;
import com.url.shortener.models.ShortenerResponse;
import com.url.shortener.service.ShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ShortenerApi REST controller.
 * <p>
 * This test class provides comprehensive coverage of all endpoints in the ShortenerApi,
 * including success and failure scenarios. It uses Mockito for mocking dependencies
 * and JUnit 5 for test execution.
 * </p>
 *
 * @author URL Shortener Team
 * @version 1.0
 * @since 2025-11-20
 */
@ExtendWith(MockitoExtension.class)
class ShortenerApiTest
{
    @Mock
    private ShortenerService shortenerService;

    @InjectMocks
    private ShortenerApi shortenerApi;

    private static final String TEST_LONG_URL = "https://example.com/very-long-url-that-needs-shortening";
    private static final String TEST_SHORT_URL = "abc123";
    private static final String EXPECTED_SHORT_URL = "http://localhost:8080/abc123";

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp()
    {
        // Additional setup if needed
    }

    /**
     * Tests the status endpoint returns correct API information.
     * <p>
     * Verifies that the root endpoint returns a descriptive message about
     * the API status and usage instructions.
     * </p>
     */
    @Test
    void testStatus_ReturnsApiInformation()
    {
        // Act
        String result = shortenerApi.status();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("URL Shortener API is running"));
        assertTrue(result.contains("/shorten"));
        assertTrue(result.contains("url"));
        assertTrue(result.contains("https://example.com/your-long-url"));
    }

    /**
     * Tests the redirect endpoint with a valid short URL.
     * <p>
     * Verifies that accessing a shortened URL returns an HTTP 302 redirect
     * with the correct Location header pointing to the original long URL.
     * </p>
     */
    @Test
    void testRedirect_WithValidShortUrl_ReturnsRedirect() throws Exception
    {
        // Arrange
        when(shortenerService.getLongUrl(TEST_SHORT_URL)).thenReturn(TEST_LONG_URL);

        // Act
        ResponseEntity response = shortenerApi.status(TEST_SHORT_URL);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(302, response.getStatusCode().value());

        HttpHeaders headers = response.getHeaders();
        assertNotNull(headers.getLocation());
        assertEquals(new URI(TEST_LONG_URL), headers.getLocation());

        verify(shortenerService, times(1)).getLongUrl(TEST_SHORT_URL);
    }

    /**
     * Tests the redirect endpoint when the service throws an exception.
     * <p>
     * Verifies that when an exception occurs during URL retrieval,
     * the endpoint returns an HTTP 500 Internal Server Error response.
     * </p>
     */
    @Test
    void testRedirect_WithServiceException_ReturnsInternalServerError()
    {
        // Arrange
        when(shortenerService.getLongUrl(anyString()))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        ResponseEntity response = shortenerApi.status("invalid");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getStatusCode().value());

        verify(shortenerService, times(1)).getLongUrl("invalid");
    }

    /**
     * Tests the redirect endpoint with a null pointer exception.
     * <p>
     * Verifies that the endpoint properly handles null pointer exceptions
     * and returns an HTTP 500 response.
     * </p>
     */
    @Test
    void testRedirect_WithNullPointerException_ReturnsInternalServerError()
    {
        // Arrange
        when(shortenerService.getLongUrl(anyString()))
                .thenThrow(new NullPointerException("Null value encountered"));

        // Act
        ResponseEntity response = shortenerApi.status("null-case");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(shortenerService, times(1)).getLongUrl("null-case");
    }

    /**
     * Tests the shorten endpoint with a valid URL.
     * <p>
     * Verifies that the endpoint accepts a long URL and returns a response
     * containing the shortened URL and a creation timestamp.
     * </p>
     */
    @Test
    void testShorten_WithValidUrl_ReturnsShortenerResponse()
    {
        // Arrange
        ShortenerRequest request = ShortenerRequest.builder()
                .url(TEST_LONG_URL)
                .build();

        when(shortenerService.getShortUrl(TEST_LONG_URL)).thenReturn(EXPECTED_SHORT_URL);

        long beforeTimestamp = System.currentTimeMillis();

        // Act
        ShortenerResponse response = shortenerApi.shorten(request);

        // Assert
        long afterTimestamp = System.currentTimeMillis();

        assertNotNull(response);
        assertEquals(EXPECTED_SHORT_URL, response.getUrl());
        assertTrue(response.getCreatedAt() >= beforeTimestamp);
        assertTrue(response.getCreatedAt() <= afterTimestamp);

        verify(shortenerService, times(1)).getShortUrl(TEST_LONG_URL);
    }

    /**
     * Tests the shorten endpoint with multiple different URLs.
     * <p>
     * Verifies that the endpoint can handle multiple requests correctly
     * and generates appropriate responses for each URL.
     * </p>
     */
    @Test
    void testShorten_WithMultipleUrls_ReturnsUniqueResponses()
    {
        // Arrange
        String url1 = "https://example.com/url1";
        String url2 = "https://example.com/url2";
        String shortUrl1 = "http://localhost:8080/short1";
        String shortUrl2 = "http://localhost:8080/short2";

        when(shortenerService.getShortUrl(url1)).thenReturn(shortUrl1);
        when(shortenerService.getShortUrl(url2)).thenReturn(shortUrl2);

        // Act
        ShortenerRequest request1 = ShortenerRequest.builder().url(url1).build();
        ShortenerRequest request2 = ShortenerRequest.builder().url(url2).build();

        ShortenerResponse response1 = shortenerApi.shorten(request1);
        ShortenerResponse response2 = shortenerApi.shorten(request2);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(shortUrl1, response1.getUrl());
        assertEquals(shortUrl2, response2.getUrl());
        assertNotEquals(response1.getUrl(), response2.getUrl());

        verify(shortenerService, times(1)).getShortUrl(url1);
        verify(shortenerService, times(1)).getShortUrl(url2);
    }

    /**
     * Tests the redirect endpoint with IllegalArgumentException.
     * <p>
     * Verifies that when the short URL is not found, the endpoint properly
     * handles IllegalArgumentException and returns HTTP 500.
     * </p>
     */
    @Test
    void testRedirect_WithIllegalArgumentException_ReturnsInternalServerError()
    {
        // Arrange
        when(shortenerService.getLongUrl(anyString()))
                .thenThrow(new IllegalArgumentException("Short URL not found"));

        // Act
        ResponseEntity response = shortenerApi.status("notfound");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(shortenerService, times(1)).getLongUrl("notfound");
    }

    /**
     * Tests the redirect endpoint with malformed URL from service.
     * <p>
     * Verifies that when the service returns an invalid URL that causes
     * URISyntaxException, the endpoint returns HTTP 500.
     * </p>
     */
    @Test
    void testRedirect_WithMalformedUrl_ReturnsInternalServerError()
    {
        // Arrange
        String malformedUrl = "ht!tp://invalid url with spaces";
        when(shortenerService.getLongUrl(TEST_SHORT_URL)).thenReturn(malformedUrl);

        // Act
        ResponseEntity response = shortenerApi.status(TEST_SHORT_URL);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(shortenerService, times(1)).getLongUrl(TEST_SHORT_URL);
    }

    /**
     * Tests the shorten endpoint with an empty URL.
     * <p>
     * Verifies that the endpoint can process requests with empty URLs
     * (though validation would typically be added in production).
     * </p>
     */
    @Test
    void testShorten_WithEmptyUrl_CallsService()
    {
        // Arrange
        String emptyUrl = "";
        ShortenerRequest request = ShortenerRequest.builder()
                .url(emptyUrl)
                .build();

        when(shortenerService.getShortUrl(emptyUrl)).thenReturn("http://localhost:8080/empty");

        // Act
        ShortenerResponse response = shortenerApi.shorten(request);

        // Assert
        assertNotNull(response);
        assertEquals("http://localhost:8080/empty", response.getUrl());
        assertTrue(response.getCreatedAt() > 0);

        verify(shortenerService, times(1)).getShortUrl(emptyUrl);
    }

    /**
     * Tests the shorten endpoint with a null URL in the request.
     * <p>
     * Verifies that the endpoint handles null URL values gracefully
     * by passing them to the service layer.
     * </p>
     */
    @Test
    void testShorten_WithNullUrl_CallsService()
    {
        // Arrange
        ShortenerRequest request = ShortenerRequest.builder()
                .url(null)
                .build();

        when(shortenerService.getShortUrl(null)).thenReturn("http://localhost:8080/null");

        // Act
        ShortenerResponse response = shortenerApi.shorten(request);

        // Assert
        assertNotNull(response);
        assertEquals("http://localhost:8080/null", response.getUrl());

        verify(shortenerService, times(1)).getShortUrl(null);
    }

    /**
     * Tests the redirect endpoint with different short URL patterns.
     * <p>
     * Verifies that the endpoint correctly handles various short URL formats.
     * </p>
     */
    @Test
    void testRedirect_WithDifferentShortUrlPatterns_ReturnsRedirect() throws Exception
    {
        // Arrange
        String[] shortUrls = {"abc", "123", "XyZ", "aB1cD2"};
        String baseLongUrl = "https://example.com/test";

        for (String shortUrl : shortUrls)
        {
            when(shortenerService.getLongUrl(shortUrl)).thenReturn(baseLongUrl + "/" + shortUrl);

            // Act
            ResponseEntity response = shortenerApi.status(shortUrl);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertEquals(new URI(baseLongUrl + "/" + shortUrl), response.getHeaders().getLocation());
        }

        verify(shortenerService, times(shortUrls.length)).getLongUrl(anyString());
    }

    /**
     * Tests the shorten endpoint verifies timestamp is generated correctly.
     * <p>
     * Verifies that each response contains a unique timestamp close to
     * the current system time.
     * </p>
     */
    @Test
    void testShorten_VerifiesTimestampGeneration()
    {
        // Arrange
        ShortenerRequest request = ShortenerRequest.builder()
                .url(TEST_LONG_URL)
                .build();

        when(shortenerService.getShortUrl(TEST_LONG_URL)).thenReturn(EXPECTED_SHORT_URL);

        // Act
        long before = System.currentTimeMillis();
        ShortenerResponse response1 = shortenerApi.shorten(request);
        ShortenerResponse response2 = shortenerApi.shorten(request);
        long after = System.currentTimeMillis();

        // Assert
        assertTrue(response1.getCreatedAt() >= before && response1.getCreatedAt() <= after);
        assertTrue(response2.getCreatedAt() >= before && response2.getCreatedAt() <= after);
        assertTrue(response2.getCreatedAt() >= response1.getCreatedAt());
    }

    /**
     * Tests the constructor of ShortenerApi.
     * <p>
     * Verifies that the controller can be properly instantiated with a service.
     * </p>
     */
    @Test
    void testConstructor_WithValidService_CreatesInstance()
    {
        // Arrange & Act
        ShortenerApi api = new ShortenerApi(shortenerService);

        // Assert
        assertNotNull(api);
    }
}
