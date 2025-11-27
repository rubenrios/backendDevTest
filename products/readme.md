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
- Swagger UI URL: `http://localhost:5000/swagger-ui.html`
- OpenAPI Specification: `http://localhost:5000/v3/api-docs`


## Architecture

The project follows a Hexagonal (Ports and Adapters) architecture:
- `domain`: Core business logic
- `application`: Application services
- `infrastructure`: Implementations, adapters, and external concerns

## Quick Start

### Clone the Repository
```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
```

### Build the Project
```bash
mvn clean install
```

### Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:5000`

## Additional Notes

- The project follows a hexagonal architecture (domain, application, infrastructure).
- The API contract is defined using OpenAPI, and DTOs are generated automatically.
- WebClient is used for outbound calls to the external product API.
- Some calls are cached using Spring Cache to improve performance and reduce latency.
- Errors from the external API are mapped to domain exceptions and handled gracefully.
- Integration and unit tests are included for the main components.