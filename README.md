# User Quota Service

## Overview

This service manages API usage quotas for users, providing rate limiting and quota enforcement. It supports both real (MySQL) and mock database operations, with the ability to switch between them based on configured time periods.

## Architecture

The application follows a layered architecture with controllers, services, and data access components:

```
┌─────────────────┐      ┌───────────────────┐      ┌─────────────────────┐
│                 │      │                   │      │                     │
│  Controllers    │─────▶│  Helper Services  │─────▶│  Business Services  │
│                 │      │                   │      │                     │
└─────────────────┘      └───────────────────┘      └─────────────────────┘
                                                              │
                                                              ▼
                                                    ┌─────────────────────┐
                                                    │                     │
                                                    │  Data Access Layer  │
                                                    │                     │
                                                    └─────────────────────┘
```

### Key Components

1. **Controllers**: Handle HTTP requests and delegate to services
   - `AdminController`: Manages quota resources and blocked users
   - `QuotaController`: Handles quota consumption requests
   - `UserController`: Manages user data

2. **Services**: Implement business logic
   - `ControllerHelperService`: Reduces controller code duplication
   - `DatabaseAwareService`: Routes operations based on active database
   - `RateLimiterService`: Enforces quota limits
   - `BlockedUserService`: Manages blocked users
   - Domain-specific services for users and quota resources

3. **Data Access Layer**: Manages database operations
   - Repository interfaces for all entities
   - Database provider implementations

## Recent Refactoring Changes

### Problem Addressed

The original controller implementations contained significant code duplication, particularly around:

1. **Database-aware operation execution** - Similar patterns for executing operations based on the active database
2. **Resource name validation** - Duplicated validation logic
3. **Error handling** - Similar try-catch blocks throughout the controllers

### Solution Implemented

A new `ControllerHelperService` was created to centralize these common patterns, resulting in:

- **Reduced controller code**: Removed duplicated try-catch blocks and validation logic
- **Improved separation of concerns**: Controllers now focus on routing and request handling
- **Centralized error handling**: Consistent error responses across all controllers
- **Enhanced maintainability**: Easier to update common logic in one place

### Implementation Details

#### ControllerHelperService

The new service provides two main methods:

1. `executeResourceOperation()`: For operations that require resource name validation
2. `executeGenericOperation()`: For operations without resource validation

Both methods handle exception catching, logging, and delegate to the `DatabaseAwareService` for database-aware execution.

#### Controller Updates

All controllers were comprehensively updated:

1. **AdminController**: Updated to use `executeResourceOperation` for endpoints that require resource validation and `executeGenericOperation` for general operations.

2. **QuotaController**: Simplified by using `executeResourceOperation` for quota consumption operations.

3. **UserController**: Completely refactored to use `executeGenericOperation` for all user management operations, while maintaining the same functionality and error handling.

In each controller, we:
- Injected the new `ControllerHelperService`
- Replaced direct calls to `DatabaseAwareService` with the new helper methods
- Removed duplicated try-catch blocks and validation logic
- Maintained consistent response formatting using `ResponseService`

## Development

### Running the Application

The application can be started using Maven:

```bash
./mvnw spring-boot:run
```

### Configuration

The application supports different database providers that are active at configured times:
- MySQL: Default real database
- NotARealDB: Mock database for testing and demonstrations

Configuration for active times is in `application.properties`.

### Profiles

- `dev`: Enables data loading for development and testing
- `prod`: Production configuration

## API Endpoints

### User Management
- `GET /v1/users`: Get all users
- `GET /v1/users/{userId}`: Get a specific user
- `POST /v1/users`: Create a new user

### Quota Management
- `POST /v1/ConsumeQuotaResourceOne`: Consume quota for resource one
- `POST /v1/ConsumeQuotaResourceTwo`: Consume quota for resource two

### Admin Operations
- `GET /v1/admin/resources`: Get information about all quota resources
- `PUT /v1/admin/resources/{resourceName}/threshold`: Update a resource threshold
- `GET /v1/admin/blocked-users`: Get all blocked users
- `GET /v1/admin/blocked-users/{resourceName}`: Get blocked users for a specific resource
- `PUT /v1/admin/blocked-users/{userId}/unblock`: Unblock a user for a specific resource

## Technologies Used

- **Java 21**: The programming language used to build the application.
- **Spring Boot 3.x**: Framework for creating standalone Java applications.
- **Spring Data JPA**: For data access with Hibernate as the ORM provider.
- **MySQL**: Primary database for the application.
- **Docker**: For containerizing the MySQL database.
- **Maven**: Dependency management and build tool.
- **JUnit 5 & Spring Boot Test**: For unit and integration testing.
- **H2 Database**: For in-memory testing.
- **Jakarta Validation**: For input validation.

## Features

- **User Management**: Create, read, update, and delete operations for users.
- **Rate Limiting**: Different thresholds for different types of resources.
- **Database Switching**: The application can switch between databases based on time of day.
- **Admin Panel**: Interface for managing quotas and viewing blocked users.
- **Persistence**: Blocked user state is maintained across application restarts.

## Project Structure

The application follows a clean architecture approach with:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain business logic
- **Repositories**: Data access layer
- **Entities**: Database model objects
- **DTOs**: Data Transfer Objects for API communication

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Docker and Docker Compose

### Running the Application

1. **Clone the repository**:
   ```
   git clone [repository-url]
   cd user-quota-service
   ```

2. **Start the MySQL database**:
   ```
   docker-compose up -d
   ```

3. **Build and run the application**:
   ```
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **Access the application**: Open your browser and navigate to http://localhost:8080

### Testing

Run the tests with:
```
./mvnw test
```

### API Endpoints

#### User Endpoints

- **GET /v1/users**: Get all users
- **GET /v1/users/{id}**: Get user by ID
- **POST /v1/users**: Create a new user
- **PUT /v1/users/{id}**: Update an existing user
- **DELETE /v1/users/{id}**: Delete a user

#### Quota Endpoints

- **POST /v1/ConsumeQuotaResourceOne?userId={id}**: Consume quota for resource one
- **POST /v1/ConsumeQuotaResourceTwo?userId={id}**: Consume quota for resource two

#### Admin Endpoints

- **GET /v1/admin/resources**: Get information about all quota resources
- **PUT /v1/admin/resources/{resourceName}/threshold?threshold={value}**: Update resource threshold
- **GET /v1/admin/blocked-users**: Get all blocked users
- **GET /v1/admin/blocked-users/{resourceName}**: Get blocked users for specific resource
- **PUT /v1/admin/blocked-users/{userId}/unblock?resourceName={name}**: Unblock a user for a resource

## Configuration

The application can be configured via the `application.properties` file:

- **Database Timing**: Configure when each database is active
  ```
  database.mysql.start-time=00:00
  database.mysql.end-time=17:59
  database.notarealdb.start-time=18:00
  database.notarealdb.end-time=23:59
  ```

## Best Practices Implemented

- **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **OOP**: Inheritance, Encapsulation, Polymorphism
- **DTOs**: For data transfer between layers
- **Input Validation**: Using Jakarta Validation
- **Proper Error Handling**: With appropriate HTTP status codes
- **Logging**: Using SLF4J for tracking application behavior
- **Testing**: Unit and integration tests
- **Documentation**: Code comments and comprehensive README 