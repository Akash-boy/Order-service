# 🛒 E-Commerce Microservices - Order Service

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=openjdk)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Enabled-black?style=flat&logo=apache-kafka)](https://kafka.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat&logo=mysql)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A robust order management microservice built with Spring Boot, featuring event-driven architecture with Kafka, RESTful APIs, and comprehensive order lifecycle management.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Related Services](#related-services)
- [Future Enhancements](#future-enhancements)
- [Contributing](#contributing)
- [Contact](#contact)

## 🎯 Overview

This is the **Order Service** component of a larger e-commerce microservices ecosystem. It handles order creation, order lifecycle management, order status updates, and publishes order events to Kafka for downstream services.

**Part of E-Commerce Microservices Suite:**
- ✅ Order Service (This repository)
- ✅ [Payment Service](https://github.com/Akash-boy/Payment-service)
- 🚧 Inventory Service (Coming soon)
- 🚧 Analytics Service (Coming soon)

## ✨ Features

### Core Features
- ✅ **Order Creation** - Create orders with validation
- ✅ **Order Status Management** - Track order lifecycle (PENDING → CONFIRMED → SHIPPED → DELIVERED)
- ✅ **Order History** - Retrieve orders by user with pagination
- ✅ **Order Cancellation** - Cancel orders with business rules validation
- ✅ **Event-Driven Architecture** - Kafka integration for order events
- ✅ **Payment Integration** - Communicates with Payment Service

### Technical Features
- ✅ **Comprehensive Validation** - Bean validation with custom business rules
- ✅ **Global Exception Handling** - Centralized error management
- ✅ **Transaction Management** - ACID compliance for order operations
- ✅ **Logging & Monitoring** - Detailed logging with SLF4J
- ✅ **RESTful API Design** - Clean, intuitive API endpoints
- ✅ **Database Indexing** - Optimized queries for performance

## 🏗️ Architecture
```
┌─────────────┐      ┌──────────────────┐      ┌─────────────┐
│   Client    │─────▶│  Order Service   │─────▶│   MySQL     │
└─────────────┘      └──────────────────┘      └─────────────┘
                              │
                              │ publishes events
                              ▼
                     ┌──────────────────┐
                     │  Apache Kafka    │
                     └──────────────────┘
                              │
                              │ consumed by
                              ▼
                     ┌──────────────────┐
                     │ Payment Service  │
                     │ Inventory Service│
                     │ Analytics Service│
                     └──────────────────┘
```

### Order Processing Flow
```
1. Client creates order
2. Order Service validates order data
3. Create order record (Status: PENDING)
4. Publish OrderCreated event to Kafka
5. Payment Service processes payment
6. Payment Service publishes PaymentCompleted event
7. Order Service updates order status (CONFIRMED)
8. Inventory Service updates stock
9. Order proceeds to SHIPPED → DELIVERED
```

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 3.x | Application Framework |
| Spring Data JPA | 3.x | Database Access |
| Hibernate | 6.x | ORM Framework |
| MySQL | 8.0 | Relational Database |
| Apache Kafka | 3.x | Message Broker |
| Lombok | 1.18.x | Boilerplate Reduction |
| Maven/Gradle | 3.8+ | Build Tool |
| SLF4J + Logback | Latest | Logging |

## 🚀 Getting Started

### Prerequisites
```bash
# Required
- Java 17 or higher
- Maven 3.8+ / Gradle 7.x+
- MySQL 8.0+
- Apache Kafka 3.x (or Docker)

# Recommended
- IntelliJ IDEA / Eclipse
- Postman (for API testing)
- Docker Desktop
```

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/Akash-boy/Order-service.git
cd Order-service
```

#### 2. Configure Database

Create MySQL database:
```sql
CREATE DATABASE order_service;
```

Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/order_service
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
```

#### 3. Start Kafka (using Docker)
```bash
# Start Zookeeper
docker run -d --name zookeeper -p 2181:2181 zookeeper

# Start Kafka
docker run -d --name kafka -p 9092:9092 \
  --link zookeeper \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka
```

#### 4. Build & Run
```bash
# Build the project (Maven)
mvn clean install

# Or build with Gradle
gradle build

# Run the application (Maven)
mvn spring-boot:run

# Or run with Gradle
gradle bootRun

# Or run the JAR
java -jar target/order-service-0.0.1-SNAPSHOT.jar
```

The service will start on `http://localhost:8081`

### Quick Test
```bash
# Health check
curl http://localhost:8081/actuator/health

# Create a test order
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 101,
        "productName": "Laptop",
        "quantity": 1,
        "price": 999.99
      }
    ],
    "totalAmount": 999.99
  }'
```

## 📚 API Documentation

### Base URL
```
http://localhost:8081/api/v1/orders
```

### Endpoints

#### 1. Create Order
```http
POST /api/v1/orders
Content-Type: application/json

{
  "userId": 1,
  "items": [
    {
      "productId": 101,
      "productName": "Laptop",
      "quantity": 1,
      "price": 999.99
    }
  ],
  "shippingAddress": "123 Main St, City, State 12345",
  "totalAmount": 999.99
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "orderId": "ORD-1234567890",
  "userId": 1,
  "items": [...],
  "totalAmount": 999.99,
  "status": "PENDING",
  "createdAt": "2026-01-29T10:30:00"
}
```

#### 2. Get Order by ID
```http
GET /api/v1/orders/{orderId}
```

#### 3. Get User Orders (Paginated)
```http
GET /api/v1/orders/user/{userId}?page=0&size=10&sortBy=createdAt&sortDirection=DESC
```

#### 4. Update Order Status
```http
PUT /api/v1/orders/{orderId}/status
Content-Type: application/json

{
  "status": "CONFIRMED"
}
```

#### 5. Cancel Order
```http
POST /api/v1/orders/{orderId}/cancel
Content-Type: application/json

{
  "reason": "Customer request"
}
```

## 📁 Project Structure
```
order-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── controller/          # REST Controllers
│   │   │   │   └── OrderController.java
│   │   │   ├── service/             # Business Logic
│   │   │   │   └── OrderService.java
│   │   │   ├── repository/          # Data Access Layer
│   │   │   │   └── OrderRepository.java
│   │   │   ├── entities/            # JPA Entities
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── OrderStatus.java
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── OrderRequest.java
│   │   │   │   └── OrderResponse.java
│   │   │   ├── exception/           # Custom Exceptions
│   │   │   │   ├── OrderException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── eventProducer/       # Kafka Producers
│   │   │   │   └── OrderEventProducer.java
│   │   │   └── eventConsumer/       # Kafka Consumers
│   │   │       └── PaymentEventConsumer.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-example.properties
│   └── test/                        # Unit & Integration Tests
├── .gitignore
├── pom.xml / build.gradle
├── README.md
├── LICENSE
└── CONTRIBUTING.md
```

## 🔗 Related Services

This Order Service works together with other microservices:

- **[Payment Service](https://github.com/Akash-boy/Payment-service)** - Handles payment processing
- **Inventory Service** (Coming soon) - Manages product stock
- **Analytics Service** (Coming soon) - Provides business insights

## 🎯 Future Enhancements

### Planned Features
- [ ] Order tracking with real-time updates
- [ ] Integration with shipping providers
- [ ] Order modification (before confirmation)
- [ ] Bulk order creation
- [ ] Order search and filtering
- [ ] Scheduled order status checks
- [ ] Order analytics dashboard
- [ ] Docker containerization
- [ ] Kubernetes deployment manifests
- [ ] Comprehensive integration tests
- [ ] Circuit breaker pattern
- [ ] API rate limiting
- [ ] OpenAPI/Swagger documentation

### Learning Goals
This project is built to learn and demonstrate:
- ✅ Microservices architecture
- ✅ Event-driven design with Kafka
- ✅ RESTful API best practices
- ✅ Service-to-service communication
- ✅ Database optimization
- 🚧 Distributed transactions (Saga pattern)
- 🚧 Container orchestration
- 🚧 CI/CD pipelines

## 🤝 Contributing

Contributions are welcome! This is a learning project, and I'm open to suggestions and improvements.

### How to Contribute

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Akash**
- GitHub: [@Akash-boy](https://github.com/Akash-boy)
- Email: your.email@example.com

## 🙏 Acknowledgments

- Spring Boot documentation
- Kafka documentation
- Stack Overflow community
- Microservices design patterns resources

## 📊 Project Status

**Current Status:** 🚧 In Active Development

**Microservices Completion:**
- Order Service: ✅ 75% Complete
- Payment Service: ✅ 80% Complete
- Inventory Service: 🚧 In Progress
- Analytics Service: 📋 Planned

---

⭐ If you found this project helpful, please give it a star!

💬 Questions? Feel free to open an issue or reach out!

**Check out my other microservices:**
- [Payment Service](https://github.com/Akash-boy/Payment-service) - Payment processing microservice