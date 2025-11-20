package com.url.shortener;

import com.url.shortener.repository.ShortenerRepository;
import com.url.shortener.service.ShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ShortenerService class.
 * <p>
 * This test class provides comprehensive coverage of all service methods,
 * including success scenarios, edge cases, and exception handling. It uses
 * Mockito for mocking the repository layer and JUnit 5 for test execution.
 * </p>
 *
 * @author URL Shortener Team
 * @version 1.0
 * @since 2025-11-20
 */
@ExtendWith(MockitoExtension.class)
class ShortenerServiceTest
{
    @Mock
    private ShortenerRepository shortenerRepository;

    @InjectMocks
    private ShortenerService shortenerService;

    private static final String DEFAULT_DOMAIN = "http://localhost:8080/";
    private static final String CUSTOM_DOMAIN = "https://short.url/";
    private static final String TEST_LONG_URL = "https://example.com/very-long-url-that-needs-to-be-shortened";
    private static final String TEST_SHORT_URL_ID = "abc123";
    private static final String TEST_SHORT_URL = "XyZ789";

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp()
    {
        // Set default domain using reflection since it's @Value annotated
        ReflectionTestUtils.setField(shortenerService, "shortUrlDomain", DEFAULT_DOMAIN);
    }

    /**
     * Tests successful URL shortening with default domain.
     * <p>
     * Verifies that the service combines the repository's short URL identifier
     * with the configured domain to create a complete shortened URL.
     * </p>
     */
    @Test
    void testGetShortUrl_WithValidUrl_ReturnsCompleteShortUrl()
    {
        // Arrange
        when(shortenerRepository.shortenUrl(TEST_LONG_URL)).thenReturn(TEST_SHORT_URL_ID);

        // Act
        String result = shortenerService.getShortUrl(TEST_LONG_URL);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_DOMAIN + TEST_SHORT_URL_ID, result);
        assertTrue(result.startsWith(DEFAULT_DOMAIN));
        assertTrue(result.endsWith(TEST_SHORT_URL_ID));
        verify(shortenerRepository, times(1)).shortenUrl(TEST_LONG_URL);
    }

    /**
     * Tests URL shortening with custom domain configuration.
     * <p>
     * Verifies that the service uses the configured custom domain
     * when generating shortened URLs.
     * </p>
     */
    @Test
    void testGetShortUrl_WithCustomDomain_ReturnsShortUrlWithCustomDomain()
    {
        // Arrange
        ReflectionTestUtils.setField(shortenerService, "shortUrlDomain", CUSTOM_DOMAIN);
        when(shortenerRepository.shortenUrl(TEST_LONG_URL)).thenReturn(TEST_SHORT_URL_ID);

        // Act
        String result = shortenerService.getShortUrl(TEST_LONG_URL);

        // Assert
        assertNotNull(result);
        assertEquals(CUSTOM_DOMAIN + TEST_SHORT_URL_ID, result);
        assertTrue(result.startsWith(CUSTOM_DOMAIN));
        assertFalse(result.startsWith(DEFAULT_DOMAIN));
        verify(shortenerRepository, times(1)).shortenUrl(TEST_LONG_URL);
    }

    /**
     * Tests URL shortening with multiple different URLs.
     * <p>
     * Verifies that the service correctly processes multiple URLs
     * and combines each with the domain prefix.
     * </p>
     */
    @Test
    void testGetShortUrl_WithMultipleUrls_ReturnsUniqueShortUrls()
    {
        // Arrange
        String url1 = "https://example.com/url1";
        String url2 = "https://example.com/url2";
        String url3 = "https://example.com/url3";

        String shortId1 = "abc";
        String shortId2 = "xyz";
        String shortId3 = "123";

        when(shortenerRepository.shortenUrl(url1)).thenReturn(shortId1);
        when(shortenerRepository.shortenUrl(url2)).thenReturn(shortId2);
        when(shortenerRepository.shortenUrl(url3)).thenReturn(shortId3);

        // Act
        String result1 = shortenerService.getShortUrl(url1);
        String result2 = shortenerService.getShortUrl(url2);
        String result3 = shortenerService.getShortUrl(url3);

        // Assert
        assertEquals(DEFAULT_DOMAIN + shortId1, result1);
        assertEquals(DEFAULT_DOMAIN + shortId2, result2);
        assertEquals(DEFAULT_DOMAIN + shortId3, result3);

        assertNotEquals(result1, result2);
        assertNotEquals(result2, result3);
        assertNotEquals(result1, result3);

        verify(shortenerRepository, times(1)).shortenUrl(url1);
        verify(shortenerRepository, times(1)).shortenUrl(url2);
        verify(shortenerRepository, times(1)).shortenUrl(url3);
    }

    /**
     * Tests URL shortening with empty URL string.
     * <p>
     * Verifies that the service delegates empty URL handling to the repository
     * and still constructs a valid short URL with the domain prefix.
     * </p>
     */
    @Test
    void testGetShortUrl_WithEmptyUrl_ReturnsShortUrl()
    {
        // Arrange
        String emptyUrl = "";
        when(shortenerRepository.shortenUrl(emptyUrl)).thenReturn(TEST_SHORT_URL_ID);

        // Act
        String result = shortenerService.getShortUrl(emptyUrl);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_DOMAIN + TEST_SHORT_URL_ID, result);
        verify(shortenerRepository, times(1)).shortenUrl(emptyUrl);
    }

    /**
     * Tests URL shortening with null URL.
     * <p>
     * Verifies that the service delegates null URL handling to the repository layer.
     * </p>
     */
    @Test
    void testGetShortUrl_WithNullUrl_CallsRepository()
    {
        // Arrange
        when(shortenerRepository.shortenUrl(null)).thenReturn(TEST_SHORT_URL_ID);

        // Act
        String result = shortenerService.getShortUrl(null);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_DOMAIN + TEST_SHORT_URL_ID, result);
        verify(shortenerRepository, times(1)).shortenUrl(null);
    }

    /**
     * Tests URL shortening when repository throws IllegalStateException.
     * <p>
     * Verifies that exceptions from the repository layer are properly
     * propagated to the caller.
     * </p>
     */
    @Test
    void testGetShortUrl_WhenRepositoryThrowsException_PropagatesException()
    {
        // Arrange
        when(shortenerRepository.shortenUrl(TEST_LONG_URL))
                .thenThrow(new IllegalStateException("Failed to generate URL ID"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> shortenerService.getShortUrl(TEST_LONG_URL)
        );

        assertEquals("Failed to generate URL ID", exception.getMessage());
        verify(shortenerRepository, times(1)).shortenUrl(TEST_LONG_URL);
    }

    /**
     * Tests URL shortening with domain without trailing slash.
     * <p>
     * Verifies that the service concatenates the short URL identifier
     * directly to the domain, regardless of trailing slashes.
     * </p>
     */
    @Test
    void testGetShortUrl_WithDomainWithoutTrailingSlash_ConcatenatesCorrectly()
    {
        // Arrange
        String domainNoSlash = "https://short.url";
        ReflectionTestUtils.setField(shortenerService, "shortUrlDomain", domainNoSlash);
        when(shortenerRepository.shortenUrl(TEST_LONG_URL)).thenReturn(TEST_SHORT_URL_ID);

        // Act
        String result = shortenerService.getShortUrl(TEST_LONG_URL);

        // Assert
        assertEquals(domainNoSlash + TEST_SHORT_URL_ID, result);
        verify(shortenerRepository, times(1)).shortenUrl(TEST_LONG_URL);
    }

    /**
     * Tests successful retrieval of long URL.
     * <p>
     * Verifies that the service delegates to the repository and returns
     * the original long URL for a given short URL identifier.
     * </p>
     */
    @Test
    void testGetLongUrl_WithValidShortUrl_ReturnsLongUrl()
    {
        // Arrange
        when(shortenerRepository.getLongUrl(TEST_SHORT_URL)).thenReturn(TEST_LONG_URL);

        // Act
        String result = shortenerService.getLongUrl(TEST_SHORT_URL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_LONG_URL, result);
        verify(shortenerRepository, times(1)).getLongUrl(TEST_SHORT_URL);
    }

    /**
     * Tests retrieval of multiple different long URLs.
     * <p>
     * Verifies that the service correctly retrieves different long URLs
     * for different short URL identifiers.
     * </p>
     */
    @Test
    void testGetLongUrl_WithMultipleShortUrls_ReturnsCorrectLongUrls()
    {
        // Arrange
        String shortUrl1 = "abc";
        String shortUrl2 = "xyz";
        String longUrl1 = "https://example.com/url1";
        String longUrl2 = "https://example.com/url2";

        when(shortenerRepository.getLongUrl(shortUrl1)).thenReturn(longUrl1);
        when(shortenerRepository.getLongUrl(shortUrl2)).thenReturn(longUrl2);

        // Act
        String result1 = shortenerService.getLongUrl(shortUrl1);
        String result2 = shortenerService.getLongUrl(shortUrl2);

        // Assert
        assertEquals(longUrl1, result1);
        assertEquals(longUrl2, result2);
        assertNotEquals(result1, result2);

        verify(shortenerRepository, times(1)).getLongUrl(shortUrl1);
        verify(shortenerRepository, times(1)).getLongUrl(shortUrl2);
    }

    /**
     * Tests retrieval with non-existent short URL.
     * <p>
     * Verifies that IllegalArgumentException from the repository is
     * properly propagated to the caller.
     * </p>
     */
    @Test
    void testGetLongUrl_WithNonExistentShortUrl_ThrowsException()
    {
        // Arrange
        String nonExistent = "notfound";
        when(shortenerRepository.getLongUrl(nonExistent))
                .thenThrow(new IllegalArgumentException("Short URL not found: " + nonExistent));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shortenerService.getLongUrl(nonExistent)
        );

        assertTrue(exception.getMessage().contains("Short URL not found"));
        assertTrue(exception.getMessage().contains(nonExistent));
        verify(shortenerRepository, times(1)).getLongUrl(nonExistent);
    }

    /**
     * Tests retrieval with empty short URL.
     * <p>
     * Verifies that the service delegates empty string handling to
     * the repository layer.
     * </p>
     */
    @Test
    void testGetLongUrl_WithEmptyShortUrl_DelegatesToRepository()
    {
        // Arrange
        String emptyShortUrl = "";
        when(shortenerRepository.getLongUrl(emptyShortUrl))
                .thenThrow(new IllegalArgumentException("Short URL not found: "));

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> shortenerService.getLongUrl(emptyShortUrl)
        );

        verify(shortenerRepository, times(1)).getLongUrl(emptyShortUrl);
    }

    /**
     * Tests retrieval with null short URL.
     * <p>
     * Verifies that the service delegates null handling to the repository layer.
     * </p>
     */
    @Test
    void testGetLongUrl_WithNullShortUrl_DelegatesToRepository()
    {
        // Arrange
        when(shortenerRepository.getLongUrl(null))
                .thenThrow(new IllegalArgumentException("Short URL not found: null"));

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> shortenerService.getLongUrl(null)
        );

        verify(shortenerRepository, times(1)).getLongUrl(null);
    }

    /**
     * Tests retrieval with various short URL patterns.
     * <p>
     * Verifies that the service correctly handles different short URL
     * formats including numeric, alphabetic, and mixed patterns.
     * </p>
     */
    @Test
    void testGetLongUrl_WithVariousShortUrlPatterns_ReturnsCorrectUrls()
    {
        // Arrange
        String[] shortUrls = {"123", "abc", "ABC", "aB1cD2", "zZZ999"};

        for (String shortUrl : shortUrls)
        {
            String expectedLongUrl = "https://example.com/" + shortUrl;
            when(shortenerRepository.getLongUrl(shortUrl)).thenReturn(expectedLongUrl);

            // Act
            String result = shortenerService.getLongUrl(shortUrl);

            // Assert
            assertEquals(expectedLongUrl, result);
        }

        verify(shortenerRepository, times(shortUrls.length)).getLongUrl(anyString());
    }

    /**
     * Tests the constructor of ShortenerService.
     * <p>
     * Verifies that the service can be properly instantiated with
     * a repository dependency.
     * </p>
     */
    @Test
    void testConstructor_WithValidRepository_CreatesInstance()
    {
        // Arrange & Act
        ShortenerService service = new ShortenerService(shortenerRepository);

        // Assert
        assertNotNull(service);
    }

    /**
     * Tests complete flow: shorten then retrieve.
     * <p>
     * Verifies the full workflow of shortening a URL and then retrieving
     * it back to ensure service layer integration works correctly.
     * </p>
     */
    @Test
    void testCompleteFlow_ShortenThenRetrieve_ReturnsOriginalUrl()
    {
        // Arrange
        when(shortenerRepository.shortenUrl(TEST_LONG_URL)).thenReturn(TEST_SHORT_URL_ID);
        when(shortenerRepository.getLongUrl(TEST_SHORT_URL_ID)).thenReturn(TEST_LONG_URL);

        // Act
        String shortUrl = shortenerService.getShortUrl(TEST_LONG_URL);
        String extractedId = shortUrl.substring(DEFAULT_DOMAIN.length());
        String retrievedLongUrl = shortenerService.getLongUrl(extractedId);

        // Assert
        assertEquals(DEFAULT_DOMAIN + TEST_SHORT_URL_ID, shortUrl);
        assertEquals(TEST_SHORT_URL_ID, extractedId);
        assertEquals(TEST_LONG_URL, retrievedLongUrl);

        verify(shortenerRepository, times(1)).shortenUrl(TEST_LONG_URL);
        verify(shortenerRepository, times(1)).getLongUrl(TEST_SHORT_URL_ID);
    }

    /**
     * Tests URL shortening with very long URLs.
     * <p>
     * Verifies that the service can handle extremely long URLs correctly.
     * </p>
     */
    @Test
    void testGetShortUrl_WithVeryLongUrl_ReturnsShortUrl()
    {
        // Arrange
        StringBuilder longUrl = new StringBuilder("https://example.com/path?");
        for (int i = 0; i < 1000; i++)
        {
            longUrl.append("param").append(i).append("=value").append(i).append("&");
        }
        String veryLongUrl = longUrl.toString();

        when(shortenerRepository.shortenUrl(veryLongUrl)).thenReturn(TEST_SHORT_URL_ID);

        // Act
        String result = shortenerService.getShortUrl(veryLongUrl);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_DOMAIN + TEST_SHORT_URL_ID, result);
        assertTrue(result.length() < veryLongUrl.length());
        verify(shortenerRepository, times(1)).shortenUrl(veryLongUrl);
    }

    /**
     * Tests URL shortening with special characters.
     * <p>
     * Verifies that the service handles URLs with query parameters,
     * hashtags, and special characters correctly.
     * </p>
     */
    @Test
    void testGetShortUrl_WithSpecialCharacters_ReturnsShortUrl()
    {
        // Arrange
        String urlWithSpecialChars = "https://example.com/search?q=test&category=books#section1";
        when(shortenerRepository.shortenUrl(urlWithSpecialChars)).thenReturn(TEST_SHORT_URL_ID);

        // Act
        String result = shortenerService.getShortUrl(urlWithSpecialChars);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_DOMAIN + TEST_SHORT_URL_ID, result);
        verify(shortenerRepository, times(1)).shortenUrl(urlWithSpecialChars);
    }

    /**
     * Tests domain configuration with different protocols.
     * <p>
     * Verifies that the service works correctly with both HTTP and HTTPS domains.
     * </p>
     */
    @Test
    void testGetShortUrl_WithDifferentProtocols_UsesConfiguredProtocol()
    {
        // Test with HTTP
        ReflectionTestUtils.setField(shortenerService, "shortUrlDomain", "http://short.url/");
        when(shortenerRepository.shortenUrl(TEST_LONG_URL)).thenReturn(TEST_SHORT_URL_ID);

        String httpResult = shortenerService.getShortUrl(TEST_LONG_URL);
        assertTrue(httpResult.startsWith("http://"));

        // Test with HTTPS
        ReflectionTestUtils.setField(shortenerService, "shortUrlDomain", "https://short.url/");
        String httpsResult = shortenerService.getShortUrl(TEST_LONG_URL);
        assertTrue(httpsResult.startsWith("https://"));

        verify(shortenerRepository, times(2)).shortenUrl(TEST_LONG_URL);
    }

    /**
     * Tests that service properly delegates to repository without modification.
     * <p>
     * Verifies that the service acts as a pure pass-through for getLongUrl,
     * not modifying the data from the repository.
     * </p>
     */
    @Test
    void testGetLongUrl_DoesNotModifyRepositoryResult()
    {
        // Arrange
        String repositoryResult = "https://example.com/original-url";
        when(shortenerRepository.getLongUrl(TEST_SHORT_URL)).thenReturn(repositoryResult);

        // Act
        String result = shortenerService.getLongUrl(TEST_SHORT_URL);

        // Assert
        assertSame(repositoryResult, result); // Should be the exact same object reference
        verify(shortenerRepository, times(1)).getLongUrl(TEST_SHORT_URL);
    }
}
