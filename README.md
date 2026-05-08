# AI Payment Fraud Detection Engine

[![CI Pipeline](https://github.com/YOUR_USERNAME/ai-fraud-detection-engine/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/ai-fraud-detection-engine/actions)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

A real-time payment fraud detection system that combines **rule-based analysis** with **AI-powered scoring** to identify fraudulent transactions in under 200ms. Built with Spring Boot 3, Apache Kafka, Redis, Spring AI, and React.

## Architecture

```
                    +------------------+
                    |   React Frontend |
                    |   (Dashboard)    |
                    +--------+---------+
                             |
                    +--------v---------+
                    |   Spring Boot    |
                    |   REST API       |
                    +--------+---------+
                             |
              +--------------+--------------+
              |                             |
     +--------v---------+        +---------v--------+
     | Rule-Based Engine|        | AI Fraud Analyzer |
     | (6 fraud rules)  |        | (Spring AI/GPT-4o)|
     +--------+---------+        +---------+--------+
              |                             |
              +--------------+--------------+
                             |
                    +--------v---------+
                    | Combined Scoring  |
                    | (40% rules +     |
                    |  60% AI)         |
                    +--------+---------+
                             |
         +-------------------+-------------------+
         |                   |                   |
+--------v------+   +-------v-------+   +-------v-------+
| PostgreSQL    |   | Apache Kafka  |   |    Redis      |
| (Persistence) |   | (Streaming)   |   | (Caching)     |
+---------------+   +---------------+   +---------------+
```

## Features

- **Real-time fraud scoring** - Transactions analyzed in <200ms
- **Hybrid detection engine** - Rule-based + AI scoring (weighted 40/60)
- **6 built-in fraud rules:**
  - High amount threshold detection
  - Transaction velocity monitoring
  - Geographic anomaly detection
  - Unknown device detection
  - Odd-hours transaction flagging
  - Amount deviation from user profile
- **AI-powered analysis** - GPT-4o contextual fraud reasoning via Spring AI
- **Kafka streaming** - Real-time transaction ingestion pipeline
- **Redis caching** - Sub-millisecond user profile lookups
- **Dashboard API** - Stats, alerts, and transaction history
- **Prometheus + Grafana** - Operational monitoring
- **Docker Compose** - One-command full stack deployment

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.3.5, Spring WebFlux |
| AI/ML | Spring AI, OpenAI GPT-4o |
| Streaming | Apache Kafka |
| Cache | Redis 7 |
| Database | PostgreSQL 16 |
| Monitoring | Prometheus, Grafana, Micrometer |
| Testing | JUnit 5, Mockito, Testcontainers |
| CI/CD | GitHub Actions |
| Containerization | Docker, Docker Compose |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Frontend | React 18, TypeScript, Tailwind CSS |

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Node.js 18+ (for frontend)

### Run with Docker Compose

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/ai-fraud-detection-engine.git
cd ai-fraud-detection-engine

# Set your OpenAI API key
export OPENAI_API_KEY=your-key-here

# Start all services
docker-compose up -d

# App runs on http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Grafana: http://localhost:3001 (admin/admin)
```

### Run Locally

```bash
# Start dependencies
docker-compose up -d postgres redis kafka zookeeper

# Run backend
./mvnw spring-boot:run

# Run frontend
cd frontend && npm install && npm run dev
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Submit a transaction for fraud analysis |
| GET | `/api/transactions` | List transactions (paginated, filterable) |
| GET | `/api/transactions/{id}` | Get transaction details |
| GET | `/api/transactions/user/{userId}` | Get user's transactions |
| GET | `/api/transactions/dashboard/stats` | Dashboard statistics |
| GET | `/api/alerts` | List fraud alerts |
| GET | `/api/alerts/user/{userId}` | User's fraud alerts |
| PATCH | `/api/alerts/{id}/status` | Update alert status |

### Example: Submit a Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "amount": 2500.00,
    "currency": "USD",
    "merchantName": "Electronics Store",
    "merchantCategory": "ELECTRONICS",
    "cardLast4": "4242",
    "country": "US",
    "city": "New York",
    "ipAddress": "192.168.1.1",
    "deviceId": "device-001"
  }'
```

### Example Response

```json
{
  "id": "a1b2c3d4-...",
  "userId": "user-123",
  "amount": 2500.00,
  "currency": "USD",
  "merchantName": "Electronics Store",
  "status": "APPROVED",
  "fraudScore": 0.23,
  "fraudReason": null,
  "timestamp": "2024-01-15T14:30:00"
}
```

## Testing

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker for Testcontainers)
./mvnw verify
```

## Project Structure

```
ai-fraud-detection-engine/
├── src/main/java/com/portfolio/frauddetection/
│   ├── config/          # Kafka, Redis, CORS configuration
│   ├── controller/      # REST API endpoints
│   ├── dto/             # Request/Response objects
│   ├── engine/          # Fraud detection core (rules + AI)
│   ├── exception/       # Global error handling
│   ├── kafka/           # Kafka producers and consumers
│   ├── model/           # JPA entities and enums
│   ├── repository/      # Data access layer
│   └── service/         # Business logic
├── src/test/            # Unit and integration tests
├── frontend/            # React dashboard
├── monitoring/          # Prometheus config
├── .github/workflows/   # CI/CD pipeline
├── docker-compose.yml   # Full stack deployment
├── Dockerfile           # Multi-stage build
└── pom.xml              # Maven dependencies
```

## License

MIT
