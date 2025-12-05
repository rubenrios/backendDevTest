# Products API

## Project Description

A reactive Product Management API built with Spring Boot and WebFlux.  
It retrieves product information from an external API and exposes
REST endpoints using a clean Hexagonal Architecture.

## Key Features

- Reactive REST API (Spring WebFlux)
- Product similarity lookup
- Resilience patterns (CircuitBreaker, Retry, RateLimiter, Timeout)
- Caching for frequently accessed product data
- WebClient-based external API integration
- Swagger/OpenAPI documentation
- Unit, integration, and end-to-end (E2E) tests

## Prerequisites

- Java 17+
- Maven 3.8+
- Spring Boot 3.4.x

## Technologies Used

- Spring Boot / Spring WebFlux
- Spring Cache
- Resilience4j (CircuitBreaker, Retry, RateLimiter)
- WebClient
- Swagger/OpenAPI
- JUnit 5, Mockito
- WireMock (E2E tests)

## API Documentation

The API exposes an OpenAPI contract and Swagger UI:

- Swagger UI: `http://localhost:5000/swagger-ui.html`
- OpenAPI Specification: `http://localhost:5000/openapi.yml`

## Architecture

This project follows a **Hexagonal (Ports & Adapters) architecture**:

- **domain** — business models, ports, domain exceptions  
- **application** — orchestrates use cases  
- **infrastructure** — adapters (REST controller, WebClient client, config)

Outbound calls to the external Product API are handled through an adapter:
`ProductExistingApiClient`, which includes caching and resilience patterns.

## Resilience

The application implements several Resilience4j patterns:

- **Circuit Breaker** to prevent cascading failures  
- **Retry** with controlled backoff  
- **Rate Limiter** to limit outbound traffic  
- **Timeouts** at WebClient and Reactor level  

Configurations are defined in `application.yml` and tuned down in
`application-test.yml` for faster and deterministic tests.

## Testing

The project includes:

- **Unit tests** for domain and application services  
- **Integration tests** for adapters  
- **End-to-end (E2E) tests** using:
  - `WebTestClient` to call the real HTTP layer  
  - `WireMock` to simulate the external Product API  

E2E tests use a dedicated `application-test.yml`.

## Quick Start

### Clone the Repository
```bash
git clone https://github.com/rubenrios/backendDevTest.git
cd backendDevTest/products
```

### Build the Project
```bash
mvn clean install
```

### Run the Application
```bash
mvn spring-boot:run
```

The API will start at:  
`http://localhost:5000`

## Additional Notes

- DTOs and API interfaces are generated from the OpenAPI contract.
- All outbound HTTP calls are handled through a single WebClient bean.
- Errors from the external API are mapped to domain exceptions and processed
  by a centralized `GlobalExceptionHandler`.
- Caching reduces repeated external calls for product details and similar IDs.
