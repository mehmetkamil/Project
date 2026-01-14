# Spring Boot Microservices Event Management System

This project is an event management system developed using Spring Boot, designed with a microservices architecture.

## Project Structure

### Monolithic Application
- **firstspingproject**: Main application service

### Microservices
- **user-service**: User management and authentication
- **event-service**: Event management
- **booking-service**: Ticket reservation system
- **payment-service**: Payment processing
- **discovery-server**: Eureka Discovery Server

## Technologies

- Java 17+
- Spring Boot 3.x
- Spring Security
- JWT Authentication
- Spring Data JPA
- PostgreSQL
- Maven
- Eureka Discovery Server

## Installation

```bash
# Clone the repository
git clone <repository-url>

# Build the microservices
cd microservices-parent
mvn clean install

# Run each service separately
cd discovery-server
mvn spring-boot:run
```

## Features

- ✅ JWT-based authentication
- ✅ Microservices architecture
- ✅ Service Discovery (Eureka)
- ✅ Security audit logging
- ✅ RESTful API
- ✅ Admin panel
