# PayFlow Gateway

A secure, enterprise-grade payment processing gateway built with Spring Boot. This system provides comprehensive payment processing capabilities with advanced security features, rate limiting, and real-time transaction management.

## Overview

PayFlow Gateway is designed to handle payment transactions securely and efficiently. It supports multiple payment methods, implements robust security measures, and provides a clean REST API for integration with e-commerce platforms and applications.

## Key Features

- **Multi-Payment Support**: Credit cards, digital wallets, and bank transfers
- **Enterprise Security**: API key authentication, JWT tokens, and request encryption
- **Rate Limiting**: Redis-based rate limiting to prevent abuse
- **Real-time Processing**: Instant transaction processing with state management
- **Comprehensive Logging**: Detailed audit trails for all transactions
- **Docker Ready**: Containerized deployment with PostgreSQL and Redis
- **Merchant Management**: Multi-tenant architecture supporting multiple merchants
- **Webhook Support**: Real-time notifications for transaction events

## Technology Stack

- **Backend**: Spring Boot 3.5.5, Java 21
- **Database**: PostgreSQL 15 with Hibernate ORM
- **Cache**: Redis 7 for caching and rate limiting
- **Security**: Spring Security with custom API key authentication
- **Build**: Maven with wrapper
- **Containerization**: Docker Compose
- **Documentation**: OpenAPI/Swagger integration

## Quick Start

### Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Git

### Installation

1. Clone the repository:
```bash
git clone https://github.com/AD404-0/payflow-gateway.git
cd payflow-gateway
```

2. Set up environment variables:
```bash
# Copy the environment template
copy .env.example .env

# Edit .env with your actual values
# Set strong passwords for database and Redis
# Generate a secure JWT secret (at least 512 bits)
```

3. Start the infrastructure:
```bash
docker-compose up -d
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080/api/v1`

## Configuration

### Environment Setup

Before running the application, you need to configure your environment:

1. **Copy configuration templates**:
   - Copy `.env.example` to `.env`
   - Copy `src/main/resources/application.properties.template` to `src/main/resources/application.properties`

2. **Set your values**:
   - Database credentials
   - Redis password
   - JWT secret key
   - API configuration

### Security Configuration

This project prioritizes security. Please review the `SECURITY-SETUP.md` file for detailed security guidelines before deployment.

## API Documentation

### Authentication

The API uses API key authentication. Include your API key in requests:

```bash
# Using X-API-Key header
curl -H "X-API-Key: your_api_key_here" http://localhost:8080/api/v1/merchants/your-merchant-id

# Using Authorization header
curl -H "Authorization: Bearer your_api_key_here" http://localhost:8080/api/v1/payments
```

### Key Endpoints

- `GET /api/v1/merchants/{id}` - Get merchant information
- `POST /api/v1/payments` - Process a payment
- `GET /api/v1/payments` - List payments
- `POST /api/v1/payments/{id}/refund` - Refund a payment
- `GET /api/v1/actuator/health` - Health check

### API Documentation

Visit `http://localhost:8080/api/v1/swagger-ui.html` for interactive API documentation.

## Development

### Project Structure

```
src/main/java/com/payflow/gateway/
├── command/          # Command pattern for payment operations
├── config/           # Configuration classes
├── controller/       # REST API controllers
├── domain/           # Domain models and enums
├── entity/           # JPA entities
├── processor/        # Payment processors
├── repository/       # Data access layer
├── security/         # Security components
├── service/          # Business logic
└── state/            # State management for transactions
```

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw clean package
```

## Deployment

### Docker Deployment

1. Build the application:
```bash
./mvnw clean package
```

2. Start all services:
```bash
docker-compose up -d
```

### Environment Variables

Set these environment variables in production:

- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `REDIS_PASSWORD` - Redis password
- `JWT_SECRET` - JWT signing secret
- `JWT_EXPIRATION` - Token expiration time

## Monitoring and Management

### Health Checks

- Application health: `GET /api/v1/actuator/health`
- Database status: Included in health endpoint
- Redis status: Included in health endpoint

### Logging

The application provides comprehensive logging:
- Transaction processing logs
- Security events
- Performance metrics
- Error tracking

### Database Management

Access PgAdmin at `http://localhost:8081` with the credentials set in your `.env` file.

## Security Considerations

- All API keys are masked in logs for security
- Real secrets are never committed to version control
- Environment variables are used for all sensitive configuration
- Rate limiting prevents API abuse
- All transactions are logged for audit purposes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions or issues:
1. Check the existing issues on GitHub
2. Review the `SECURITY-SETUP.md` documentation
3. Create a new issue with detailed information

## Acknowledgments

Built with Spring Boot and modern Java practices for enterprise-grade payment processing.