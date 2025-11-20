package com.url.shortener.repository;

import com.url.shortener.utilities.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Repository class for managing URL shortening operations with Redis.
 * <p>
 * This repository provides data access methods for storing and retrieving URL mappings
 * using Redis as the backing data store. It uses a global counter to generate unique
 * identifiers for shortened URLs and encodes them using Base62 encoding.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *     <li>Generates unique short URLs using an auto-incrementing counter</li>
 *     <li>Stores URL mappings in Redis with "url:" prefix</li>
 *     <li>Uses Base62 encoding for compact URL representations</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 * @since 2025-11-20
 */
@Repository
public class ShortenerRepository
{
    /**
     * The Redis key used for the global URL ID counter.
     * <p>
     * This counter is atomically incremented for each new URL shortening request.
     * Default value is "global:url:id" if not configured in application properties.
     * </p>
     */
    @Value("${global_url_id_key:global:url:id}")
    private String key;

    /**
     * Redis template for performing operations on the Redis data store.
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Constructs a new ShortenerRepository with the specified Redis template.
     *
     * @param redisTemplate the Redis template for data store operations
     */
    public ShortenerRepository(RedisTemplate<String, Object> redisTemplate)
    {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Shortens a long URL by generating a unique short URL identifier.
     * <p>
     * This method atomically increments a global counter in Redis, encodes the count
     * using Base62 encoding to create a compact short URL, and stores the mapping
     * between the short URL and the original long URL.
     * </p>
     *
     * @param longUrl the original long URL to be shortened
     * @return the shortened URL identifier (Base62-encoded counter value)
     * @throws IllegalStateException if the counter increment operation fails
     */
    public String shortenUrl(String longUrl)
    {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null)
        {
            throw new IllegalStateException("Failed to generate URL ID");
        }
        String shortUrl = Base62.encode(count);
        redisTemplate.opsForValue().set("url:" + shortUrl, longUrl);
        return shortUrl;
    }

    /**
     * Retrieves the original long URL associated with a shortened URL.
     * <p>
     * This method looks up the URL mapping in Redis using the "url:" prefix
     * combined with the short URL identifier.
     * </p>
     *
     * @param shortUrl the shortened URL identifier
     * @return the original long URL
     * @throws IllegalArgumentException if the short URL is not found in the data store
     */
    public String getLongUrl(String shortUrl)
    {
        String longUrl = (String) redisTemplate.opsForValue().get("url:" + shortUrl);
        if (longUrl == null)
        {
            throw new IllegalArgumentException("Short URL not found: " + shortUrl);
        }
        return longUrl;
    }
}
