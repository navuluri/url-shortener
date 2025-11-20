package com.url.shortener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Main Spring Boot application class for the URL Shortener service.
 * <p>
 * This class serves as the entry point for the URL shortening application and provides
 * Spring configuration for Redis integration. It configures the Redis template with
 * appropriate serializers and initializes the global URL counter with a starting offset.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *     <li>Configures Redis connection and serialization</li>
 *     <li>Initializes the global URL ID counter with a starting value of 100,000</li>
 *     <li>Enables Spring Boot auto-configuration</li>
 *     <li>Provides RedisTemplate bean for URL data operations</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 * @since 2025-11-20
 */
@SpringBootApplication
public class UrlShortenerApplication
{
    /**
     * The Redis key used for the global URL ID counter.
     * <p>
     * This key is used to store and retrieve the auto-incrementing counter value
     * for generating unique URL identifiers. The default value is "global:url:id"
     * if not configured in application properties.
     * </p>
     */
    @Value("${global_url_id_key:global:url:id}")
    private String key;

    /**
     * Main entry point for the Spring Boot application.
     * <p>
     * This method launches the Spring Boot application, initializing all configured
     * beans and starting the embedded web server.
     * </p>
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args)
    {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }

    /**
     * Creates and configures a RedisTemplate bean for Redis operations.
     * <p>
     * This method configures a RedisTemplate with appropriate serializers:
     * <ul>
     *     <li>StringRedisSerializer for keys</li>
     *     <li>GenericToStringSerializer for values</li>
     * </ul>
     * After initialization, it sets the starting offset for the global URL counter
     * to 100,000 if it doesn't already exist in Redis.
     * </p>
     *
     * @param connectionFactory the Redis connection factory provided by Spring Boot auto-configuration
     * @return a fully configured RedisTemplate instance
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.afterPropertiesSet();
        setOffset(template);
        return template;
    }

    /**
     * Initializes the global URL ID counter in Redis with a starting offset.
     * <p>
     * This method sets the initial value of the global counter to 100,000 if the key
     * doesn't already exist in Redis. Using a starting offset ensures that shortened
     * URLs have a minimum length, making them more consistent and professional-looking.
     * The operation is atomic and will only set the value if it doesn't already exist,
     * preventing resets on application restarts.
     * </p>
     *
     * @param redisTemplate the configured Redis template for performing the operation
     */
    private void setOffset(RedisTemplate<String, Object> redisTemplate)
    {
        redisTemplate.opsForValue().setIfAbsent(key, 100000);
    }

}
