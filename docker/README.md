# PayFlow Gateway - Docker Setup

This directory contains Docker configuration for running PayFlow Gateway with PostgreSQL in a production-like environment.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)

## Quick Start

1. **Start the database services:**
   ```bash
   docker-compose up -d
   ```

2. **Check if services are running:**
   ```bash
   docker-compose ps
   ```

3. **View logs:**
   ```bash
   docker-compose logs -f postgres
   ```

4. **Stop services:**
   ```bash
   docker-compose down
   ```

## Services Included

### PostgreSQL Database
- **Port:** 5432
- **Database:** payflowdb
- **Username:** payflow_user
- **Password:** payflow_secure_password
- **Features:** 
  - Data persistence with Docker volumes
  - Health checks
  - Database initialization scripts
  - Optimized configuration for payment processing

### Redis Cache
- **Port:** 6379
- **Password:** payflow_redis_password
- **Features:**
  - Persistence enabled
  - Used for session storage and rate limiting

### PgAdmin (Database Management)
- **Port:** 8081
- **Email:** admin@payflow.com
- **Password:** admin123
- **URL:** http://localhost:8081

## Database Connection

Once services are running, update your `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payflowdb
spring.datasource.username=payflow_user
spring.datasource.password=payflow_secure_password
```

## Production Considerations

1. **Change default passwords** in docker-compose.yml
2. **Use Docker secrets** for sensitive data
3. **Configure backup strategy** for postgres_data volume
4. **Set up monitoring** with tools like Prometheus
5. **Use SSL/TLS** for database connections in production

## Volume Management

Data is persisted in Docker volumes:
- `postgres_data`: PostgreSQL data
- `redis_data`: Redis data
- `pgadmin_data`: PgAdmin configuration

To backup data:
```bash
docker run --rm -v payflow_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz /data
```

## Troubleshooting

1. **Port conflicts:** Make sure ports 5432, 6379, and 8081 are not used by other services
2. **Database connection issues:** Check if containers are running with `docker-compose ps`
3. **Permission issues:** Ensure Docker has proper permissions to create volumes

## Development vs Production

This setup is optimized for development and testing. For production:
- Use managed database services (AWS RDS, Google Cloud SQL)
- Implement proper backup and disaster recovery
- Set up database replicas for read scaling
- Use connection pooling at the application level
- Monitor database performance and optimize queries