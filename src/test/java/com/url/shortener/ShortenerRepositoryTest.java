package com.url.shortener;

import com.url.shortener.repository.ShortenerRepository;
import com.url.shortener.utilities.Base62;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ShortenerRepository class.
 * <p>
 * This test class provides comprehensive coverage of all repository methods,
 * including success scenarios, edge cases, and exception handling. It uses
 * Mockito for mocking Redis operations and JUnit 5 for test execution.
 * </p>
 *
 * @author URL Shortener Team
 * @version 1.0
 * @since 2025-11-20
 */
@ExtendWith(MockitoExtension.class)
class ShortenerRepositoryTest
{
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ShortenerRepository shortenerRepository;

    private static final String TEST_KEY = "global:url:id";
    private static final String TEST_LONG_URL = "https://example.com/very-long-url-that-needs-to-be-shortened";
    private static final String TEST_SHORT_URL = "abc123";
    private static final Long TEST_COUNTER = 100000L;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp()
    {
        // Set the key field using reflection since it's @Value annotated
        ReflectionTestUtils.setField(shortenerRepository, "key", TEST_KEY);

        // Mock the valueOperations() method
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Tests successful URL shortening operation.
     * <p>
     * Verifies that the repository increments the counter, encodes it using Base62,
     * stores the mapping, and returns the short URL.
     * </p>
     */
    @Test
    void testShortenUrl_WithValidUrl_ReturnsShortUrl()
    {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(TEST_COUNTER);
        String expectedShortUrl = Base62.encode(TEST_COUNTER);

        // Act
        String result = shortenerRepository.shortenUrl(TEST_LONG_URL);

        // Assert
        assertNotNull(result);
        assertEquals(expectedShortUrl, result);
        verify(valueOperations, times(1)).increment(TEST_KEY);
        verify(valueOperations, times(1)).set("url:" + expectedShortUrl, TEST_LONG_URL);
    }

    /**
     * Tests URL shortening with multiple sequential calls.
     * <p>
     * Verifies that each call generates a unique short URL based on
     * incrementing counter values.
     * </p>
     */
    @Test
    void testShortenUrl_WithMultipleCalls_ReturnsUniqueShortUrls()
    {
        // Arrange
        String url1 = "https://example.com/url1";
        String url2 = "https://example.com/url2";
        String url3 = "https://example.com/url3";

        when(valueOperations.increment(TEST_KEY))
                .thenReturn(100001L)
                .thenReturn(100002L)
                .thenReturn(100003L);

        // Act
        String shortUrl1 = shortenerRepository.shortenUrl(url1);
        String shortUrl2 = shortenerRepository.shortenUrl(url2);
        String shortUrl3 = shortenerRepository.shortenUrl(url3);

        // Assert
        assertNotNull(shortUrl1);
        assertNotNull(shortUrl2);
        assertNotNull(shortUrl3);
        assertNotEquals(shortUrl1, shortUrl2);
        assertNotEquals(shortUrl2, shortUrl3);
        assertNotEquals(shortUrl1, shortUrl3);

        verify(valueOperations, times(3)).increment(TEST_KEY);
        verify(valueOperations, times(1)).set("url:" + shortUrl1, url1);
        verify(valueOperations, times(1)).set("url:" + shortUrl2, url2);
        verify(valueOperations, times(1)).set("url:" + shortUrl3, url3);
    }

    /**
     * Tests URL shortening when Redis increment returns null.
     * <p>
     * Verifies that an IllegalStateException is thrown when the counter
     * increment operation fails.
     * </p>
     */
    @Test
    void testShortenUrl_WhenIncrementReturnsNull_ThrowsIllegalStateException()
    {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> shortenerRepository.shortenUrl(TEST_LONG_URL)
        );

        assertEquals("Failed to generate URL ID", exception.getMessage());
        verify(valueOperations, times(1)).increment(TEST_KEY);
        verify(valueOperations, never()).set(anyString(), any());
    }

    /**
     * Tests URL shortening with empty URL string.
     * <p>
     * Verifies that the repository can handle empty URLs (though validation
     * would typically be added at a higher layer).
     * </p>
     */
    @Test
    void testShortenUrl_WithEmptyUrl_ReturnsShortUrl()
    {
        // Arrange
        String emptyUrl = "";
        when(valueOperations.increment(TEST_KEY)).thenReturn(TEST_COUNTER);
        String expectedShortUrl = Base62.encode(TEST_COUNTER);

        // Act
        String result = shortenerRepository.shortenUrl(emptyUrl);

        // Assert
        assertNotNull(result);
        assertEquals(expectedShortUrl, result);
        verify(valueOperations, times(1)).set("url:" + expectedShortUrl, emptyUrl);
    }

    /**
     * Tests URL shortening with null URL.
     * <p>
     * Verifies that the repository processes null URLs without throwing
     * exceptions at the repository layer.
     * </p>
     */
    @Test
    void testShortenUrl_WithNullUrl_ReturnsShortUrl()
    {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(TEST_COUNTER);
        String expectedShortUrl = Base62.encode(TEST_COUNTER);

        // Act
        String result = shortenerRepository.shortenUrl(null);

        // Assert
        assertNotNull(result);
        assertEquals(expectedShortUrl, result);
        verify(valueOperations, times(1)).set("url:" + expectedShortUrl, null);
    }

    /**
     * Tests URL shortening with very large counter values.
     * <p>
     * Verifies that the repository handles large counter values correctly
     * and produces valid Base62-encoded short URLs.
     * </p>
     */
    @Test
    void testShortenUrl_WithLargeCounterValue_ReturnsValidShortUrl()
    {
        // Arrange
        Long largeCounter = 999999999L;
        when(valueOperations.increment(TEST_KEY)).thenReturn(largeCounter);
        String expectedShortUrl = Base62.encode(largeCounter);

        // Act
        String result = shortenerRepository.shortenUrl(TEST_LONG_URL);

        // Assert
        assertNotNull(result);
        assertEquals(expectedShortUrl, result);
        assertFalse(result.isEmpty());
        verify(valueOperations, times(1)).set("url:" + expectedShortUrl, TEST_LONG_URL);
    }

    /**
     * Tests successful retrieval of long URL.
     * <p>
     * Verifies that the repository retrieves the correct long URL
     * from Redis when given a valid short URL.
     * </p>
     */
    @Test
    void testGetLongUrl_WithValidShortUrl_ReturnsLongUrl()
    {
        // Arrange
        when(valueOperations.get("url:" + TEST_SHORT_URL)).thenReturn(TEST_LONG_URL);

        // Act
        String result = shortenerRepository.getLongUrl(TEST_SHORT_URL);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_LONG_URL, result);
        verify(valueOperations, times(1)).get("url:" + TEST_SHORT_URL);
    }

    /**
     * Tests retrieval of multiple different URLs.
     * <p>
     * Verifies that the repository correctly retrieves different long URLs
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

        when(valueOperations.get("url:" + shortUrl1)).thenReturn(longUrl1);
        when(valueOperations.get("url:" + shortUrl2)).thenReturn(longUrl2);

        // Act
        String result1 = shortenerRepository.getLongUrl(shortUrl1);
        String result2 = shortenerRepository.getLongUrl(shortUrl2);

        // Assert
        assertEquals(longUrl1, result1);
        assertEquals(longUrl2, result2);
        assertNotEquals(result1, result2);
        verify(valueOperations, times(1)).get("url:" + shortUrl1);
        verify(valueOperations, times(1)).get("url:" + shortUrl2);
    }

    /**
     * Tests retrieval when short URL is not found.
     * <p>
     * Verifies that an IllegalArgumentException is thrown when attempting
     * to retrieve a non-existent short URL.
     * </p>
     */
    @Test
    void testGetLongUrl_WithNonExistentShortUrl_ThrowsIllegalArgumentException()
    {
        // Arrange
        String nonExistentShortUrl = "notfound";
        when(valueOperations.get("url:" + nonExistentShortUrl)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shortenerRepository.getLongUrl(nonExistentShortUrl)
        );

        assertTrue(exception.getMessage().contains("Short URL not found"));
        assertTrue(exception.getMessage().contains(nonExistentShortUrl));
        verify(valueOperations, times(1)).get("url:" + nonExistentShortUrl);
    }

    /**
     * Tests retrieval with various short URL patterns.
     * <p>
     * Verifies that the repository correctly handles different short URL
     * formats including alphanumeric combinations.
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
            when(valueOperations.get("url:" + shortUrl)).thenReturn(expectedLongUrl);

            // Act
            String result = shortenerRepository.getLongUrl(shortUrl);

            // Assert
            assertEquals(expectedLongUrl, result);
        }

        verify(valueOperations, times(shortUrls.length)).get(anyString());
    }

    /**
     * Tests the constructor of ShortenerRepository.
     * <p>
     * Verifies that the repository can be properly instantiated with
     * a RedisTemplate.
     * </p>
     */
    @Test
    void testConstructor_WithValidRedisTemplate_CreatesInstance()
    {
        // Arrange & Act
        ShortenerRepository repository = new ShortenerRepository(redisTemplate);

        // Assert
        assertNotNull(repository);
    }

    /**
     * Tests complete flow: shorten then retrieve.
     * <p>
     * Verifies the full workflow of shortening a URL and then retrieving
     * it back to ensure data integrity.
     * </p>
     */
    @Test
    void testCompleteFlow_ShortenThenRetrieve_ReturnsOriginalUrl()
    {
        // Arrange
        when(valueOperations.increment(TEST_KEY)).thenReturn(TEST_COUNTER);
        String expectedShortUrl = Base62.encode(TEST_COUNTER);
        when(valueOperations.get("url:" + expectedShortUrl)).thenReturn(TEST_LONG_URL);

        // Act
        String shortUrl = shortenerRepository.shortenUrl(TEST_LONG_URL);
        String retrievedLongUrl = shortenerRepository.getLongUrl(shortUrl);

        // Assert
        assertEquals(expectedShortUrl, shortUrl);
        assertEquals(TEST_LONG_URL, retrievedLongUrl);
        verify(valueOperations, times(1)).increment(TEST_KEY);
        verify(valueOperations, times(1)).set("url:" + expectedShortUrl, TEST_LONG_URL);
        verify(valueOperations, times(1)).get("url:" + expectedShortUrl);
    }

    /**
     * Tests URL shortening with special characters in URL.
     * <p>
     * Verifies that the repository correctly handles URLs with query
     * parameters and special characters.
     * </p>
     */
    @Test
    void testShortenUrl_WithSpecialCharactersInUrl_ReturnsShortUrl()
    {
        // Arrange
        String urlWithSpecialChars = "https://example.com/search?q=test&category=books#section1";
        when(valueOperations.increment(TEST_KEY)).thenReturn(TEST_COUNTER);
        String expectedShortUrl = Base62.encode(TEST_COUNTER);

        // Act
        String result = shortenerRepository.shortenUrl(urlWithSpecialChars);

        // Assert
        assertNotNull(result);
        assertEquals(expectedShortUrl, result);
        verify(valueOperations, times(1)).set("url:" + expectedShortUrl, urlWithSpecialChars);
    }

    /**
     * Tests retrieval with empty short URL.
     * <p>
     * Verifies that the repository handles empty short URL strings
     * and throws an appropriate exception.
     * </p>
     */
    @Test
    void testGetLongUrl_WithEmptyShortUrl_ThrowsException()
    {
        // Arrange
        String emptyShortUrl = "";
        when(valueOperations.get("url:" + emptyShortUrl)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> shortenerRepository.getLongUrl(emptyShortUrl)
        );

        assertTrue(exception.getMessage().contains("Short URL not found"));
        verify(valueOperations, times(1)).get("url:" + emptyShortUrl);
    }

    /**
     * Tests that Redis key prefix is correctly applied.
     * <p>
     * Verifies that all Redis operations use the correct "url:" prefix
     * for storing and retrieving URL mappings.
     * </p>
     */
    @Test
    void testRedisKeyPrefix_IsCorrectlyApplied()
    {
        // Arrange
        String shortUrl = "test123";
        when(valueOperations.increment(TEST_KEY)).thenReturn(100000L);
        when(valueOperations.get("url:" + shortUrl)).thenReturn(TEST_LONG_URL);

        // Act
        shortenerRepository.shortenUrl(TEST_LONG_URL);
        shortenerRepository.getLongUrl(shortUrl);

        // Assert
        verify(valueOperations).set(startsWith("url:"), anyString());
        verify(valueOperations).get(startsWith("url:"));
    }
}
