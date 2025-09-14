# Project: Url Shortener
The challenge is to write a small Java program using the Spring Boot framework that meets the following specifications.
Features:
- Given a full URL, generate a shortened URL.
- Given a previously generated shortened URL, provide the full (original) URL.
Constraints:
- Not including the service URL (domain), a shortened URL should not exceed 10 characters.
- If the shortened URL provided to the system does not match a previously generated URL, an error must be returned.
- The system must be persistent, able to survive a reboot.
- In other words, if I generate a shortened URL using the program, I should be able to close the program, restart it, and be able to obtain the previously shortened URL.
- Two identical full URLs must result in the same shortened URL.

# URL Shortener Microservice

https://shortenworld.com/blog/the-role-of-base62-encoding-in-url-shortening-algorithms

A URL shortener service built with Java, Spring Boot and MariaDB persistence.


# Features

- **URL Shortening**: Generate short URLs with customizable length (max 10 characters)
- **URL Expansion**: Retrieve original URLs from short urls
- **Persistence**: MariaDB database 

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controller    │───▶│    Service      │───▶│   Repository    │
│   (REST API)    │    │   (Business)    │    │   (Data)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       │
                                                       │
                                                       │
                                                       │
                                                       │
                                              ┌─────────────────┐
                                              │    MariaDB      │
                                              │   (Primary)     │
                                              └─────────────────┘
```


## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- MariaDB 10.11+

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/olgagrinberg/urlshortener.git
   cd urlshortener
   ```

2. **Docker Compose commands - Useful to test the application outside the IDE**
   ```bash
   cd /path/to/your/project/urlshortener/spring-boot-docker-app
   docker compose up
   docker compose up -d
   docker compose down
   docker compose up --build
   docker compose logs
   docker compose ps
   ```

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Start the application**
   ```bash
   mvn spring-boot:run 
   ```

5. **Access the application**
   - API: http://localhost:8080
   - Controller Health Check: http://localhost:8080/api/url/health
   - Health Check: http://localhost:8080/actuator/health
   - Metrics: http://localhost:8080/actuator/metrics

## Curl examples
#### Shorten URL
    curl -X POST http://localhost:8080/api/url/shorten \
    -H "Content-Type: application/json" \
    -d '{
    "fullUrl": "https://example.com/some/page",
    "shortUrl": ""
    }'
**Response:**
```json
{
  "id": null,
  "fullUrl": "https://example.com/some/page10",
  "shortUrl": "1JnA4Cgzvj",
  "error": null
}
```

#### Expand URL
    curl -X GET http://localhost:8080/api/url/expand/1JnA4CgzqS
**Response:**
```json
{
  "id": null,
  "fullUrl": "https://example.com/some/page10",
  "shortUrl": "1JnA4Cgzvj",
  "error": null
}
```

**Error Response:**
```json
{
  "id": null,
  "fullUrl": null,
  "shortUrl": null,
  "error": "Short URL not found"
}
```
