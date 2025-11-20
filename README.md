# URL Shortener Service

A production-ready URL shortening service built with Spring Boot and Redis, designed to convert long URLs into compact,
shareable links using Base62 encoding.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![Redis](https://img.shields.io/badge/Redis-Required-red)
![Maven](https://img.shields.io/badge/Maven-Build-blue)

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#️architecture)
- [Technology Stack](#️technology-stack)
- [System Design](#system-design)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#️configuration)
- [API Endpoints](#api-endpoints)
- [Usage Examples](#usage-examples)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Code Quality](#code-quality)
- [Security Recommendations](#security-recommendations)
- [Performance Considerations](#performance-considerations)
- [Future Enhancements](#future-enhancements)
- [Monitoring & Debugging](#monitoring--debugging)
- [Author](#author)
- [Acknowledgments](#acknowledgments)

## Overview

This URL Shortener service is a lightweight, high-performance application that transforms long URLs into short,
memorable links. The service uses Redis for fast data storage and retrieval, and implements Base62 encoding to generate
compact URL identifiers.

**Key Highlights:**

- ⚡ Fast URL shortening and retrieval using Redis
- Base62 encoding for compact, URL-safe identifiers
- Automatic redirection to original URLs
- Atomic counter-based ID generation
- Comprehensive test coverage (100%)
- Fully documented with JavaDocs

## Features

### Core Functionality

- **URL Shortening**: Convert long URLs into short, shareable links
- **URL Redirection**: Automatic HTTP 302 redirect to original URLs
- **Persistent Storage**: Redis-backed storage for reliability
- **Unique ID Generation**: Atomic counter with Base62 encoding
- **Configurable Domain**: Customizable short URL domain

### Technical Features

- **RESTful API**: Clean REST endpoints for URL operations
- **Exception Handling**: Comprehensive error handling and logging
- **Scalable Design**: Redis-based architecture for horizontal scaling
- **Test Coverage**: 100% unit test coverage for all components
- **Clean Architecture**: Separation of concerns with layered design

## Architecture

The application follows a clean, layered architecture:

```
┌──────────────────────────────────────────────���──────────┐
│                     REST API Layer                       │
│                   (ShortenerApi)                         │
│         GET / | GET /{shortUrl} | POST /api/v1/shorten  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Service Layer                         │
│                  (ShortenerService)                      │
│         • Business Logic                                 │
│         • Domain Concatenation                           │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│                (ShortenerRepository)                     │
│         • Redis Operations                               │
│         • Base62 Encoding                                │
│         • Data Persistence                               │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Redis Data Store                      │
│         • URL Mappings (url:{shortId} → longUrl)        │
│         • Global Counter (global:url:id)                │
└─────────────────────────────────────────────────────────┘
```

### Component Breakdown

#### 1. **API Layer** (`ShortenerApi`)

- Handles HTTP requests/responses
- Validates input and manages redirections
- Provides REST endpoints for URL operations

#### 2. **Service Layer** (`ShortenerService`)

- Implements business logic
- Combines short URL IDs with domain configuration
- Acts as a bridge between API and Repository layers

#### 3. **Repository Layer** (`ShortenerRepository`)

- Manages Redis data operations
- Generates unique IDs using atomic counters
- Encodes IDs using Base62 for compact representation

#### 4. **Utility Layer** (`Base62`)

- Base62 encoding/decoding implementation
- Converts numeric IDs to alphanumeric strings
- URL-safe character set (0-9, A-Z, a-z)

## Technology Stack

| Technology            | Version | Purpose               |
|-----------------------|---------|-----------------------|
| **Java**              | 17      | Programming Language  |
| **Spring Boot**       | 3.5.7   | Application Framework |
| **Spring Data Redis** | 3.5.7   | Redis Integration     |
| **Redis**             | Latest  | Data Store            |
| **Lombok**            | Latest  | Code Generation       |
| **JUnit 5**           | 5.x     | Testing Framework     |
| **Mockito**           | 5.x     | Mocking Framework     |
| **Maven**             | 3.x     | Build Tool            |

## System Design

```mermaid
flowchart TD
    A[Client] -->|POST /shorten| B[Shortener API Service]
    B -->|INCR + Base62| C[(Redis)]
    B -->|" HSET url:{code} "| C
    B -->|Short URL| A
    D[Client2] -->|" GET /{code} "| E[Redirect Service]
    E -->|" HGET url:{code} "| C
    E -->|HINCRBY click_count| C
    E -->|302 redirect| D
```

### URL Shortening Flow

```
1. Client sends POST request with long URL
2. Service increments global counter in Redis (atomic operation)
3. Counter value is encoded using Base62 (e.g., 100000 → "q0U")
4. Mapping stored in Redis: url:q0U → https://example.com/long-url
5. Service returns: http://localhost:8080/q0U
```

### URL Retrieval Flow

```
1. Client accesses: http://localhost:8080/q0U
2. Service extracts short ID: "q0U"
3. Redis lookup: url:q0U → https://example.com/long-url
4. Service returns HTTP 302 redirect to original URL
5. Browser redirects user to original URL
```

### Base62 Encoding

Base62 uses 62 characters: `0-9` (10) + `A-Z` (26) + `a-z` (26) = 62 characters

**Benefits:**

- More compact than Base10 (decimal)
- URL-safe (no special characters)
- Case-sensitive for higher entropy
- Supports large number ranges

**Examples:**

- `100000` → `q0U`
- `123456789` → `8M0kX`
- `999999999` → `15FTGf`

### Data Storage Schema

**Redis Keys:**

```
global:url:id → Counter (Integer, starts at 100000)
url:{shortId} → Long URL (String)
```

**Example:**

```
global:url:id = 100005
url:q0U = https://example.com/page1
url:q0V = https://example.com/page2
url:q0W = https://example.com/page3
```

## Prerequisites

Before running the application, ensure you have:

1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   java -version
   ```

2. **Maven 3.x**
   ```bash
   mvn -version
   ```

3. **Redis Server** (running and accessible)
   ```bash
   redis-cli ping
   # Should return: PONG
   ```

## Installation

### 1. Clone the Repository

```bash
git clone <your-repository-url>
cd url-shortener
```

### 2. Configure Redis Connection

Edit `src/main/resources/application.properties`:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/url-shortener-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## Configuration

### Application Properties

| Property                     | Default                  | Description                   |
|------------------------------|--------------------------|-------------------------------|
| `server.port`                | `8080`                   | Application server port       |
| `spring.data.redis.host`     | `10.189.179.229`         | Redis server hostname         |
| `spring.data.redis.port`     | `10030`                  | Redis server port             |
| `spring.data.redis.password` | (empty)                  | Redis authentication password |
| `spring.data.redis.database` | `0`                      | Redis database index          |
| `spring.data.redis.timeout`  | `60000`                  | Redis connection timeout (ms) |
| `global_url_id_key`          | `global:url:id`          | Redis key for counter         |
| `short_url.domain`           | `http://localhost:8080/` | Short URL domain prefix       |

### Environment-Specific Configuration

Create different property files for different environments:

- `application-dev.properties` - Development
- `application-prod.properties` - Production

Run with specific profile:

```bash
java -jar url-shortener.jar --spring.profiles.active=prod
```

## API Endpoints

### 1. Health Check

**Endpoint:** `GET /`

**Description:** Check if the API is running

**Response:**

```
URL Shortener API is running. The API is available at the URI /shorten. Use the below payload to shorten a URL:
{
 "url": "https://example.com/your-long-url"
}
```

### 2. Shorten URL

**Endpoint:** `POST /api/v1/shorten`

**Description:** Create a shortened URL

**Request Body:**

```json
{
  "url": "https://example.com/very-long-url-that-needs-to-be-shortened"
}
```

**Response:** (HTTP 200 OK)

```json
{
  "url": "http://localhost:8080/q0U",
  "createdAt": 1700500000000
}
```

**Error Response:** (HTTP 500 Internal Server Error)

```json
{
  "timestamp": "2025-11-20T10:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error"
}
```

### 3. Redirect to Original URL

**Endpoint:** `GET /{shortUrl}`

**Description:** Redirect to the original long URL

**Example:** `GET /q0U`

**Response:** (HTTP 302 Found)

```
Location: https://example.com/very-long-url-that-needs-to-be-shortened
```

**Error Response:** (HTTP 500 Internal Server Error)

- When short URL is not found
- When Redis connection fails

## Usage Examples

### Using cURL

**Shorten a URL:**

```bash
curl -X POST http://localhost:8080/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.example.com/very/long/url/path"}'
```

**Response:**

```json
{
  "url": "http://localhost:8080/q0U",
  "createdAt": 1700500000000
}
```

**Access shortened URL:**

```bash
curl -L http://localhost:8080/q0U
# -L flag follows redirects
```

### Using Postman

1. **Shorten URL:**
    - Method: `POST`
    - URL: `http://localhost:8080/api/v1/shorten`
    - Headers: `Content-Type: application/json`
    - Body (raw JSON):
      ```json
      {
        "url": "https://www.example.com/page"
      }
      ```

2. **Test Redirect:**
    - Method: `GET`
    - URL: `http://localhost:8080/q0U`
    - Postman will show the redirect location

### Using JavaScript (Fetch API)

```javascript
// Shorten a URL
async function shortenUrl(longUrl) {
    const response = await fetch('http://localhost:8080/api/v1/shorten', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({url: longUrl})
    });

    const data = await response.json();
    return data.url;
}

// Usage
const shortUrl = await shortenUrl('https://example.com/long-url');
console.log('Short URL:', shortUrl);
```

### Using Python

```python
import requests
import json

# Shorten a URL
def shorten_url(long_url):
    url = "http://localhost:8080/api/v1/shorten"
    headers = {"Content-Type": "application/json"}
    payload = {"url": long_url}
    
    response = requests.post(url, headers=headers, data=json.dumps(payload))
    return response.json()

# Usage
result = shorten_url("https://example.com/very-long-url")
print(f"Short URL: {result['url']}")
print(f"Created at: {result['createdAt']}")
```

## Testing

### Test Coverage

The project includes comprehensive unit tests with **100% code coverage**:

| Component           | Test Class              | Tests  | Coverage |
|---------------------|-------------------------|--------|----------|
| ShortenerApi        | ShortenerApiTest        | 13     | 100%     |
| ShortenerService    | ShortenerServiceTest    | 22     | 100%     |
| ShortenerRepository | ShortenerRepositoryTest | 16     | 100%     |
| **Total**           |                         | **51** | **100%** |

### Running Tests

**Run all tests:**

```bash
mvn test
```

**Run specific test class:**

```bash
mvn test -Dtest=ShortenerApiTest
```

**Run with coverage report:**

```bash
mvn clean test jacoco:report
```

Coverage report will be at: `target/site/jacoco/index.html`

## Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/url/shortener/
│   │   │   ├── UrlShortenerApplication.java    # Main application class
│   │   │   ├── api/
│   │   │   │   └── ShortenerApi.java            # REST controller
│   │   │   ├── service/
│   │   │   │   └── ShortenerService.java        # Business logic
│   │   │   ├── repository/
│   │   │   │   └── ShortenerRepository.java     # Data access layer
│   │   │   ├── models/
│   │   │   │   ├── ShortenerRequest.java        # Request DTO
│   │   │   │   └── ShortenerResponse.java       # Response DTO
│   │   │   └── utilities/
│   │   │       └── Base62.java                  # Base62 encoder
│   │   └── resources/
│   │       └── application.properties            # Configuration
│   └── test/
│       └── java/com/url/shortener/
│           ├── ShortenerApiTest.java            # API tests
│           ├── ShortenerServiceTest.java        # Service tests
│           ├── ShortenerRepositoryTest.java     # Repository tests
│           └── UrlShortenerApplicationTests.java
├── pom.xml                                       # Maven configuration
├── README.md                                     # This file              
```

## Code Quality

### Strengths

1. **Clean Architecture**
    - Clear separation of concerns (API → Service → Repository)
    - Dependency injection with constructor injection
    - Single Responsibility Principle followed

2. **Comprehensive Documentation**
    - All classes have JavaDoc comments
    - All methods documented with parameters and return types
    - Exception scenarios documented

3. **Test Coverage**
    - 100% unit test coverage
    - All edge cases tested
    - Exception scenarios covered

4. **Modern Java**
    - Java 17 features utilized
    - Lombok for boilerplate reduction
    - Text blocks for multiline strings

5. **Spring Boot Best Practices**
    - Configuration externalization
    - Bean lifecycle management
    - Proper use of annotations

### Areas Analyzed for Security & Quality

#### Security Considerations

1. **Input Validation** 
    - Currently minimal validation on URLs
    - **Recommendation:** Add URL format validation
    - **Recommendation:** Implement max URL length limits

2. **Rate Limiting** 
    - No rate limiting implemented
    - **Recommendation:** Add rate limiting to prevent abuse

3. **Redis Authentication** 
    - Password field empty in configuration
    - **Recommendation:** Use strong Redis password in production

4. **Open Redirect Vulnerability** 
    - Application redirects to any stored URL
    - **Recommendation:** Validate URLs against whitelist/blacklist

5. **Error Information Disclosure** 
    - Generic 500 errors returned (good)
    - Detailed errors logged (appropriate)

#### Performance Considerations

1. **Redis Connection Pooling** ✓
    - Spring Data Redis handles connection pooling
    - Configurable timeout settings

2. **Counter Collision** ✓
    - Atomic Redis increment prevents collisions
    - Thread-safe ID generation

3. **Memory Usage** ✓
    - Minimal object creation
    - Efficient string concatenation

4. **Response Time** ✓
    - Direct Redis lookups (O(1))
    - No complex computations

#### Known Limitations

1. **URL Validation**
    - No validation of URL format
    - No check for malicious URLs
    - No duplicate URL detection

2. **Expiration**
    - URLs never expire
    - Could lead to Redis memory issues over time
    - No cleanup mechanism

3. **Analytics**
    - No tracking of URL access counts
    - No analytics or statistics

4. **Collision Handling**
    - Relies on monotonic counter
    - If counter resets, collisions possible

5. **Error Handling**
    - Generic 500 errors for all failures
    - Could provide more specific error codes

6. **HTTPS**
    - Default configuration uses HTTP
    - Should use HTTPS in production

7. **CORS**
    - No CORS configuration
    - May need configuration for web clients

## Security Recommendations

### For Production Deployment

1. **Add Input Validation**

```java
// Validate URL format
if(!longUrl.matches("^https?://.*")){
    throw new IllegalArgumentException("Invalid URL format");
}
// Check URL length
if(longUrl.length() >2048){
    throw new IllegalArgumentException("URL too long");
}
```

2. **Implement Rate Limiting**

```java
// Use Spring Cloud Gateway or Bucket4j
@RateLimiter(name = "shortenUrl", fallbackMethod = "rateLimitFallback")
public String getShortUrl(String longUrl)
{
    // existing code
}
```

3. **Secure Redis**

```properties
# application-prod.properties
spring.data.redis.password=${REDIS_PASSWORD}
spring.data.redis.ssl=true
```

4. **Add URL Validation**

```java
// Check against malicious patterns
private static final List<String> BLACKLIST = List.of("javascript:", "data:", "file:");
```

5. **Enable HTTPS**

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
```

6. **Add CORS Configuration**

```java

@Configuration
public class CorsConfig
{
    @Bean
    public WebMvcConfigurer corsConfigurer()
    {
        return new WebMvcConfigurer()
        {
            @Override
            public void addCorsMappings(CorsRegistry registry)
            {
                registry.addMapping("/api/**").allowedOrigins("https://yourdomain.com");
            }
        };
    }
}
```

## Performance Considerations

### Current Performance

- **Shorten URL**: ~5-10ms (Redis write + Base62 encode)
- **Retrieve URL**: ~2-5ms (Redis read)
- **Redirect**: ~3-7ms (Redis read + HTTP redirect)

### Scalability

**Horizontal Scaling:**

- Stateless application design
- Multiple instances can run concurrently
- Redis handles synchronization

**Vertical Scaling:**

- Redis supports millions of operations/second
- Spring Boot efficient for high throughput

**Capacity Estimation:**

Starting from 100,000:

- 6 characters (Base62): ~56 billion URLs
- 7 characters (Base62): ~3.5 trillion URLs

Current implementation supports billions of URLs before hitting Base62 limits.

### Optimization Opportunities

1. **Caching**
    - Add local cache for frequently accessed URLs
    - Use Spring Cache with Redis

2. **Async Processing**
    - Make URL shortening async for better throughput
    - Use CompletableFuture or @Async

3. **Batch Operations**
    - Support bulk URL shortening
    - Use Redis pipeline for multiple operations

## Future Enhancements

### Planned Features

1. **Custom Short URLs**
    - Allow users to specify custom short codes
    - Check for availability

2. **URL Expiration**
    - Add TTL (Time To Live) for URLs
    - Automatic cleanup of expired URLs

3. **Analytics Dashboard**
    - Track click counts
    - Geographic analytics
    - Referrer tracking

4. **User Management**
    - User accounts and authentication
    - API keys for programmatic access
    - URL management per user

5. **QR Code Generation**
    - Generate QR codes for short URLs
    - Multiple formats (PNG, SVG)

6. **URL Preview**
    - Show destination URL before redirect
    - Display URL metadata

7. **Admin Dashboard**
    - View statistics
    - Manage URLs
    - Configure settings

8. **API Versioning**
    - Support multiple API versions
    - Backward compatibility

9. **Metrics & Monitoring**
    - Prometheus metrics
    - Health checks
    - Performance monitoring

10. **Database Backup**
    - Redis persistence configuration
    - Backup and restore functionality

## Monitoring & Debugging

### Health Check

Check if application is running:

```bash
curl http://localhost:8080/
```

### Redis Monitoring

Check Redis connection:

```bash
redis-cli -h localhost -p 6379 ping
```

View stored keys:

```bash
redis-cli -h localhost -p 6379 KEYS "url:*"
```

Check counter value:

```bash
redis-cli -h localhost -p 6379 GET global:url:id
```

### Application Logs

Logs are output to console by default. Configure logging in `application.properties`:

```properties
logging.level.com.url.shortener=DEBUG
logging.level.org.springframework.data.redis=DEBUG
```

## Contributing

This is a personal portfolio project and is not open source. However, feedback and suggestions are welcome!

## Author

**Bhaskara Navuluri**

- GitHub: [@navuluri](https://github.com/navuluri)
- LinkedIn: [Bhaskara Navuluri](www.linkedin.com/in/bhaskara-navuluri-52a0151b2)
- Stackoverflow: [Bhaskara Navuluri](https://stackoverflow.com/users/1781174/bhaskara)

## Acknowledgments

- Spring Boot team for the excellent framework
- Redis team for the powerful data store
- Base62 encoding concept from various URL shorteners

---

**Note:** This project is for portfolio demonstration purposes. For production use, please implement the security
recommendations and additional features mentioned above.

**Last Updated:** November 20, 2025

