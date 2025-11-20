package com.url.shortener.api;

import com.url.shortener.models.ShortenerRequest;
import com.url.shortener.models.ShortenerResponse;
import com.url.shortener.service.ShortenerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REST API controller for URL shortening and redirection services.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *     <li>Shortening long URLs into compact, shareable links</li>
 *     <li>Redirecting shortened URLs to their original destinations</li>
 *     <li>Checking the API status</li>
 * </ul>
 * </p>
 *
 * @version 1.0
 * @since 2025-11-20
 */
@RestController
@Log4j2
public class ShortenerApi
{
    private final ShortenerService shortenerService;

    /**
     * Constructs a new ShortenerApi with the specified shortener service.
     *
     * @param shortenerService the service responsible for URL shortening and retrieval operations
     */
    public ShortenerApi(ShortenerService shortenerService)
    {
        this.shortenerService = shortenerService;
    }

    /**
     * Returns the API status and usage information.
     * <p>
     * This endpoint provides basic information about the API and instructions
     * for using the URL shortening service.
     * </p>
     *
     * @return a string containing API status and usage instructions
     */
    @GetMapping("/")
    public String status()
    {
        return """
            URL Shortener API is running. The API is available at the URI /shorten. Use the below payload to shorten a URL:
            {
             "url": "https://example.com/your-long-url"
            }
            """;
    }

    /**
     * Redirects a shortened URL to its original long URL.
     * <p>
     * When a valid shortened URL is accessed, this endpoint retrieves the original URL
     * and returns an HTTP 302 redirect response. If an error occurs during the retrieval
     * process, an HTTP 500 Internal Server Error is returned.
     * </p>
     *
     * @param shortUrl the shortened URL identifier
     * @return a ResponseEntity with HTTP 302 status and Location header pointing to the original URL,
     *         or HTTP 500 status if an error occurs
     */
    @GetMapping("/{shortUrl}")
    public ResponseEntity status(@PathVariable("shortUrl") String shortUrl)
    {
        try
        {
            String longUrl = shortenerService.getLongUrl(shortUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(new URI(longUrl));
            return ResponseEntity.status(302).headers(headers).build();
        } catch (Exception e)
        {
            log.error("There is an internal server error: ", e);
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Creates a shortened URL from a long URL.
     * <p>
     * This endpoint accepts a POST request with a JSON payload containing the long URL
     * to be shortened. It returns a response containing the shortened URL and the timestamp
     * of when it was created.
     * </p>
     *
     * @param shortenerRequest the request object containing the long URL to be shortened
     * @return a ShortenerResponse containing the shortened URL and creation timestamp
     */
    @PostMapping("/api/v1/shorten")
    public ShortenerResponse shorten(@RequestBody ShortenerRequest shortenerRequest)
    {
        String shortUrl = shortenerService.getShortUrl(shortenerRequest.getUrl());
        return ShortenerResponse.builder().url(shortUrl).createdAt(System.currentTimeMillis()).build();
    }

}
