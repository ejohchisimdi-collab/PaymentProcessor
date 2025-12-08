# Payment Processor API
 A robust, payment processing system built with Spring Boot,has 40 tests and handles credit card and bank account transactions, refunds, fraud detection, and webhook notifications.
 
 # [All Endpoints ](https://ejohchisimdi-collab.github.io/PaymentProcessor/)

# Features
## Core Payment Processing
- Multiple Payment Methods: Support for credit cards and bank accounts
- Real-time Payment Processing: Immediate authorization and settlement for credit cards
- Deferred Bank Processing: Fraud validation with scheduled settlement for bank transfers
- Idempotency Support: Prevents duplicate transactions using idempotency keys
## Security & Fraud Detection
- JWT Authentication: Secure token-based authentication with role-based access control
## Intelligent Fraud Detection: Multi-factor fraud scoring system including:
- Location-based validation
- Transaction velocity checks
- Consecutive failed payment detection
- Insufficient funds prevention

## Role-Based Access Control: Separate permissions for Admins, Merchants, and Customers
### Merchant Features
- Flexible Refund Policies: Support for both partial and complete refunds
- Webhook Notifications: Real-time event notifications with automatic retry mechanism
- Customizable Settings: Per-merchant currency and transaction limits
- Merchant Accounts: Automatic settlement to merchant balance
Reliability & Resilience
- Optimistic Locking: Prevents race conditions using JPA versioning
- Retry Mechanisms: Automatic retries with exponential backoff for failed operations
- Webhook Reliability: Failed webhooks are queued and retried up to 5 times
- Transaction Management: ACID compliance for all financial operations
```
 Architecture
┌─────────────────┐
│ Controllers │ ← REST API Layer (JWT Protected)
└────────┬────────┘
         │
┌────────▼────────┐
│ Services │ ← Business Logic & Validation
└────────┬────────┘
         │
┌────────▼────────┐
│ Repositories │ ← Data Access Layer (JPA)
└────────┬────────┘
         │
┌────────▼────────┐
│ MySQL DB │ ← Persistent Storage
└─────────────────┘
```
## Key Components
### Payment Processing Pipeline:
- Pending → Initial payment creation
- Validated → Fraud checks completed
- Authorized → Funds reserved
- Captured → Funds captured
- Settled → Transferred to merchant account
### Refund Processing:
- Pending → Refund request validation
- Processing → Policy checks (partial/complete)
- Settlement → Funds returned to customer

### Tech Stack
- Backend Framework: Spring Boot 3.x
- Security: Spring Security + JWT
- Database: MySQL 8.0
- ORM: Spring Data JPA (Hibernate)
- Build Tool: Maven
- Containerization: Docker & Docker Compose
- Testing: JUnit 5, Mockito
- API Documentation: Swagger/OpenAPI
- Logging: SLF4J + Logback


## Getting Started
- Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0
- Docker & Docker Compose (optional)
### Option 1: Docker (Recommended)
- Clone the repository
```
git clone https://github.com/ejohchisimdi-collab/PaymentProcessor.git
```
- cd payment-processor
- Start the application
```
docker-compose up -d
```
- The API will be available at http://localhost:8080

### Option 2: Local Development
- Clone and configure
```
git clone https://github.com/yourusername/payment-processor.git
```
- cd payment-processor
- Set up MySQL database
```
CREATE DATABASE paymentProcessor;
CREATE USER 'paymentuser'@'localhost' IDENTIFIED BY 'paymentpass';
GRANT ALL PRIVILEGES ON paymentProcessor.* TO 'paymentuser'@'localhost';
```

- Configure environment variables
```
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/paymentProcessor
export SPRING_DATASOURCE_USERNAME=paymentuser
export SPRING_DATASOURCE_PASSWORD=paymentpass
export JWT_SECRET=YourSecretKeyThatIsAtLeast32CharactersLong
export JWT_EXPIRATION=86400000
```
- Build and run
```
./mvnw clean install
./mvnw spring-boot:run
```

### Default Admin Account
- On first startup, a default admin account is created:
- Username: Admin
- Password: Admin
- Role: Admin
-  Change this immediately in production!

## Payment Flow
- Credit Card Payment Flow
- Customer Payment → PENDING → VALIDATED → AUTHORIZED → CAPTURED → SETTLED
```
                      ↓ ↓ ↓ ↓ ↓
                   Webhook Webhook Webhook Webhook Webhook
                              (Fraud (Funds (Funds (Merchant
                              Checks) Hold) Capture) Receives)
Bank Account Payment Flow
Customer Payment → CAPTURED (Immediate) → Daily Validation → SETTLED or FAILED
                      ↓ ↓ ↓
                   Webhook Fraud Checks Webhook
                                         (Scheduled)
```

##  Fraud Detection
### The system implements a comprehensive fraud scoring mechanism:
- Check
- Score
- Description:
```
Different Location: Payment from unusual location:
+1

High Velocity:3+ payments in < 1 minute:
+1

Failed History: 3 consecutive failed payments:
+1

Insufficient Funds:
Auto-Fail

Fraud score greater than or equal to three:
auto fail
```
Balance < transaction amount
 Fraud Threshold: Score ≥ 3 = Payment Failed

 ```
Example: Fraud Detection in Action
// Scenario: Customer makes 3 rapid payments from different location
Payment 1: Location = USA, Time = 10:00:00 → SUCCESS
Payment 2: Location = USA, Time = 10:00:30 → SUCCESS  
Payment 3: Location = France, Time = 10:00:45 → WARNING (different location +1)
Payment 4: Location = France, Time = 10:00:55 → FAILED (high velocity +1, total = 2)
```

# Configuration

# Application Properties

## Database Configuration

spring.datasource.url= {SPRING_DATASOURCE_URL}
spring.datasource.username={SPRING_DATASOURCE_USERNAME}
spring.datasource.password={SPRING_DATASOURCE_PASSWORD}

## JPA Configuration
- spring.jpa.hibernate.ddl-auto=update
- spring.jpa.show-sql=true

##  JWT Configuration
- jwt.secret={JWT_SECRET}
- jwt.expiration={JWT_EXPIRATION} # in milliseconds (86400000 = 24 hours)

# Scheduled Jobs
## The system runs scheduled jobs for:
- Bank Payment Validation: Daily at midnight (00:00)
- Webhook Retries: Daily at midnight (00:00)

# Testing
- Run All Tests
- ./mvnw test
## Test Coverage
Service Layer: 45 Comprehensive unit tests with Mockito


# Deployment
## Docker Deployment
The application includes a multi-stage Dockerfile for optimized builds:
# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
#  build application

# Production stage
FROM eclipse-temurin:17-jre-alpine
# Run application
- Docker Compose
- Complete stack with MySQL:
- docker-compose up -d
## Services:
- payment-api: Port 8080
- mysql: Port 3307 (mapped from 3306)




# License
- This project is licensed under the MIT License - see the LICENSE file for details.
# Author
- Chisimdi Ejoh
- LinkedIn: www.linkedin.com/in/chisimdi-ejoh-057ba1382
GitHub: @yourusername

# Acknowledgments
- Spring Boot team for the excellent framework
The open-source community
- Stripe for the inspiration
- Disclaimer: This is a demonstration project for portfolio purposes. For production use, additional security measures, PCI DSS compliance, and proper legal compliance are required.

