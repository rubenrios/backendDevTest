# Products API

## Project Description

A Product Management API

## Key Features

- Product querying
- RESTful API endpoints
- Swagger/OpenAPI documentation

## Prerequisites

- Java 17+
- Maven 3.8+
- Spring Boot 3.4.x or higher

## Technologies Used

- Spring Boot
- Swagger/OpenAPI
- JUnit 5
- Mockito

## API Documentation

API documentation is available via Swagger:
- Swagger UI URL: `http://localhost:8080/swagger-ui.html`
- OpenAPI Specification: `http://localhost:8080/v3/api-docs`


## Architecture

The project follows a Hexagonal (Ports and Adapters) architecture:
- `domain`: Core business logic
- `application`: Application services
- `infrastructure`: Implementations, adapters, and external concerns
